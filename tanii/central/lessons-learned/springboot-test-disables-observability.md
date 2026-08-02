# Lesson — @SpringBootTest disables metrics/tracing autoconfig by default

## What happened

An integration test hitting `/actuator/prometheus` got a **404**, even though the Prometheus registry was on the classpath and the endpoint was exposed.

## Why

`@SpringBootTest` (mock environment) **does not auto-configure metrics or tracing by default** — to keep tests light. So the Prometheus scrape endpoint isn't registered, and no `Tracer`/`Propagator` beans exist.

## How to avoid

Add **`@AutoConfigureObservability`** to any test that needs the metrics endpoint or a real tracer. Conversely, code that constructor-injects tracing beans should treat them as optional (e.g. `ObjectProvider`) so tests without that annotation still wire up.
