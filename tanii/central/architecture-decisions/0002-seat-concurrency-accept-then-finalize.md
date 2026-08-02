# ADR-0002 — Seat concurrency: accept-then-finalize + optimistic lock

Status: Accepted

## Context

Many students may confirm the **last seat** of a class at the same time. The system must never hand out one seat too many, and must respond quickly (the confirm is user-facing).

## Decision

**Accept-then-finalize.** `confirm` returns **202 Accepted**, moves the enrollment to `PROCESSING`, and writes a *finalize command* to a transactional **outbox** — all in one DB transaction. A finalize consumer then secures the seat with an **optimistic lock** on the `Class` seat counter (`version`) plus **bounded retry**; when seats are gone the enrollment becomes `REJECTED`. **PostgreSQL is the single arbiter** — no distributed lock.

## Rationale

- Instant response; the seat is secured asynchronously.
- The guarantee lives in the database (ACID + `@Version`), which is an honest demonstration of concurrency control.
- Competing consumers on the finalize work queue create the real race; a serial consumer would hide it.

## Consequences

- Requires an outbox + relay and idempotent consumers (`event_id`).
- Proven by a deterministic test: N simultaneous confirmations for the last seat → exactly one `CONFIRMED`, the rest `REJECTED`.
- A seat is held only while `CONFIRMED`; cancelling a confirmed enrollment releases it.

## Related

- Broker choice: [ADR-0001](0001-messaging-broker-rabbitmq.md).
- Keeping the trace across the async boundary: [outbox-trace-carry](../patterns/outbox-trace-carry.md).
