# Observability

This service is built to answer three questions when something goes wrong in production, each backed
by one of the three pillars of observability:

| Question | Pillar | What it is |
| --- | --- | --- |
| *What happened in this specific event?* | **Logs** | one text record per event |
| *How is the system doing overall?* (throughput, latency, memory) | **Metrics** | numbers over time |
| *Where did this one request go, end to end?* | **Traces** | the path of a single request across services |

Each dependency we added maps to one pillar.

## Logs — `logstash-logback-encoder`

By default Spring logs plain text, which reads well in a terminal but is hard to query later. This
encoder switches the output to **structured JSON**, so every field (`traceId`, `service`, `level`,
and any business context) becomes searchable in a log aggregator (Loki, Elasticsearch, Datadog):

```json
{"@timestamp":"...","level":"INFO","message":"Enrollment created","service":"academic-service","traceId":"a1b2c3"}
```

The encoder includes the SLF4J MDC, and the tracing bridge places `traceId`/`spanId` in the MDC — so
every line automatically carries the trace it belongs to.

## Metrics — `micrometer-registry-prometheus`

Spring Boot already collects metrics internally via Micrometer (request counts, latency, JVM memory,
connection pools). This dependency **exposes** them in the format Prometheus scrapes, at
`/actuator/prometheus`:

```
http_server_requests_seconds_count{uri="/api/enrollments",status="201"} 42
jvm_memory_used_bytes{area="heap"} 1.34e8
```

Without it, that endpoint returns 404. It is purely the translator from Micrometer's internal meters
to the Prometheus dialect.

## Traces — `micrometer-tracing-bridge-brave`

A trace follows **one** request across every hop it touches:

```
POST /enrollments → write to DB → publish to RabbitMQ → notifications consumes → append audit
```

Two ids make that possible:

- **`traceId`** — a unique id generated at the start of a request that travels with it the whole way.
  It is the "tracking number": every log and step of the same journey shares it.
- **`spanId`** — each *step* within the journey (the HTTP span, the publish span, the consume span)
  has its own `spanId`, but all of them share the same `traceId`.

The bridge is the engine that (1) generates these ids per request, (2) puts them in the MDC so logs
carry them, and (3) **propagates** the `traceId` across boundaries by injecting it into outgoing
message headers, so the next service continues the same trace. (*Brave* is the concrete
implementation; *micrometer-tracing* is the abstract API; `-bridge-brave` wires the two together.)

`management.tracing.sampling.probability` is set to `1.0` so **every** request is traced in this demo
(in production you would sample a fraction to control overhead).

## Why the trace context is carried through the outbox

Propagation is normally automatic: when service A calls service B synchronously, the `traceId` rides
along in the request header and B continues the same trace. Our enrollment flow, however, is
**asynchronous** because of the transactional outbox — and that breaks the automatic propagation:

```
[HTTP request thread]                 [Relay thread, ~1s later]
POST /confirm                         (the request has already returned and is gone)
  ├─ traceId = X  ✅                    relay wakes on its schedule
  ├─ write a row to the outbox         ├─ reads the outbox row
  └─ respond 202 and end              └─ publishes to RabbitMQ  ← no active trace here
```

When the relay publishes, it runs on a scheduled thread with **no active request** — the original
`traceId = X` died with the HTTP thread. Left alone, the relay would start a brand-new trace `Y`, and
the consumer would log `Y` with no link back to the `/confirm` that caused it. The chain would break
exactly at the asynchronous boundary.

The `outbox.trace_context` column fixes this by storing the "tracking number" alongside the message:

1. **On write** (inside the request, `traceId = X` is alive): the current trace context is captured
   and saved in `trace_context` — like stapling the tracking number to the parcel before shelving it.
2. **On publish** (relay, no trace): the context is read back and **restored** before publishing, so
   the publish continues the original trace `X`.
3. **On consume**: Brave already propagates `X` through the RabbitMQ message headers, so
   notifications-service logs with `traceId = X` too.

The mechanism lives in
[`TracePropagation`](../services/academic-service/src/main/java/com/enrollment/academic/shared/observability/TracePropagation.java)
(`capture()` / `continueTrace(...)`), used by `OutboxWriter` and `OutboxRelay`. The tracing beans are
optional (`ObjectProvider`), so the code degrades to a no-op when tracing is disabled.

**Result:** searching a log aggregator for `traceId = X` shows the whole journey — the `POST /confirm`,
the relay's publish, and the notifications consumer — even though it crossed the database and the
broker asynchronously.

## Operational endpoints

| Endpoint | Purpose |
| --- | --- |
| `/actuator/health` | liveness/readiness (public) |
| `/actuator/prometheus` | metrics scrape target |
| `/actuator/info` | build/app info |

## How to verify locally

1. Run the stack (see the root `README`).
2. Make an enrollment and confirm it.
3. Grep the aggregated JSON logs for a single `traceId` and confirm the same value appears in both
   `academic-service` (request + relay publish) and `notifications-service` (consumer) lines.
