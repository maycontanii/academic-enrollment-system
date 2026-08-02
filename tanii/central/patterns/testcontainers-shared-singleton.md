# Pattern — Shared singleton Testcontainers + per-test reset

## Problem

Starting real infra (PostgreSQL, RabbitMQ) per test class is slow; sharing one instance across tests leaks state between them (row counts, unique constraints, queued messages).

## Approach

- Start **one static singleton** of each container for the whole JVM test run (started once, reused by every integration test via a shared base class). Wire Spring to it with `@DynamicPropertySource`.
- Restore isolation with a **`@BeforeEach` reset**: truncate the domain tables and purge the queues, so each test starts from a clean slate.
- Set the broker to dead-letter failed messages instead of requeueing forever, so a poisoned message in one test can't loop into the next.

## Result

Fast (containers boot once) and isolated (clean state per test) — without per-class container churn.

## When to use

Integration suites that exercise real datastore/broker behavior rather than mocks.
