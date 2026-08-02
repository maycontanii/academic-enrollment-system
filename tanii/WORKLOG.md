# Worklog

Ephemeral session state for **this project**. Where Tier 0 changes are logged and rough notes live before anything durable is captured into `central/`.

Rules:

- One line per Tier 0 change: `YYYY-MM-DD — what changed`.
- Never store permanent knowledge here. Approved, reusable knowledge belongs in `central/`.
- Safe to prune. Nothing here is a source of truth.

## Log

- 2026-08-02 — Captured durable knowledge into `central/` after the implementation phases: ADR-0002 (seat concurrency), ADR-0003 (JIT identity linking), ADR-0004 (gateway for scaling); patterns (authorize-by-action, outbox trace-carry, shared-singleton Testcontainers); lessons (SpringBootTest observability autoconfig, Keycloak two-hostname JWKS).
