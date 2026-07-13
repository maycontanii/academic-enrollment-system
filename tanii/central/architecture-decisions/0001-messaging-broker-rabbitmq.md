# ADR-0001 — Messaging broker: RabbitMQ

Status: Accepted (design-time)

## Context

Enrollment finalization needs asynchronous command processing (competing consumers) and domain events fanned out to notifications and audit. Volume is bounded and bursty (registration windows). The priorities are: no request lost, each processed exactly once, and failures visible — not replaying large event histories.

## Decision

Use **RabbitMQ**. Not Kafka, for now. In an AWS-native deployment the equivalent shape is **SNS (fan-out) + SQS (work queue)**.

## Rationale

- **Workload fit** — task distribution + pub/sub fan-out is RabbitMQ's native model: queues with competing consumers, exchanges for broadcast, per-message ack, native retry and dead-lettering.
- **RabbitMQ ≈ SNS + SQS in one broker** — a queue behaves like SQS, an exchange like SNS. The design maps cleanly to AWS later with no shape change. Self-hosted RabbitMQ is used purely for local reproducibility.
- **Kafka is a different tool** — a retained, partitioned commit log for very high throughput, replay and stream processing. None of those are needed here; choosing it would be unjustified complexity.
- **Decisive point** — seat integrity is demonstrated by concurrent consumers contending at the database, resolved with optimistic locking. Kafka's idiomatic answer (partition by class id) would serialize finalizations per class and dissolve the race at the routing layer — moving the guarantee out of the database and changing what the system demonstrates.

## Consequences

- Simple local Docker Compose; DLQ and retry are first-class.
- The concurrency guarantee stays in the database — an honest demonstration of concurrency control.

## When we'd revisit (→ Kafka)

- Sustained high-throughput event streams.
- Replaying the full event history (event sourcing, rebuilding read models, late-joining consumers).
- Many independent consumer groups reading the same immutable stream.
- Stream processing / analytics over the event flow.
