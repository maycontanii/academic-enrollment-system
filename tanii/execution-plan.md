# Execution Plan — Academic Enrollment System

## Metadata

- Status: Approved
- Tier: 2
- Mode: AI Architect

## Approved Inputs

- Domain Map — approved.
- PRD — approved.
- UI Skeleton — approved.
- Technical Design — approved.

## Goal

Implement the system incrementally, **core-first**: the mandatory domain and the seat-integrity guarantee are complete and proven before differentials are layered on. At every point the build is runnable and the critical tests are green.

## Tasks

### Phase 1 — Foundation

- [x] **T1. Initialize repository structure** — set up the monorepo before anything else: create `services/` (backend) and `web/` (frontend) folders, a root `docker-compose.yml` (empty to start), `.gitignore`, and a README stub. *Done: the structure exists and the empty Compose file parses.*
- [x] **T2. Provision backing infra** — the root `docker-compose.yml` stands up the environment before any app code: `postgres` (init two databases `academicdb`, `notificationsdb`), `rabbitmq` (management UI; exchanges/queues/DLQ declared by the app on startup), `keycloak` (import a realm export: client + composite roles `ADMIN`/`STUDENT` with the fine-grained action rules + a couple of test users). App services are added to this same file as they are built (T3, T10, T13). *Done: `docker compose up` brings postgres + rabbitmq + keycloak healthy; the realm is imported.*
- [x] **T3. Scaffold academic-service** — under `services/`; Spring Boot, layered (`controller` / `application` / `domain` / `repository` / `dto`); Flyway baseline; wired to the T2 infra. *Done: app boots against the stack, `/actuator/health` green, baseline migration applies.*
- [x] **T4. Aggregates & persistence** — Student, Course, Subject, Class (`seat_limit`, `seats_used`, `status`, `version`), Enrollment (`status`, timestamps, partial-unique active `(student_id, class_id)`); migrations + repositories. *Done: tables created by migration; repository CRUD unit-tested.*

### Phase 2 — Core domain (mandatory)

- [x] **T5. Catalog REST + CRUD** — students, courses, subjects, classes; `open`/`close` class; input validation + standardized error envelope; springdoc OpenAPI served. *Done: API/integration tests green; `/v3/api-docs` present.*
- [x] **T6. Enrollment creation & queries** — create PENDING; rules: open class only, no duplicate active; consult by student and by class (paged). *Done: unit + API tests for each rule.*
- [x] **T7. Seat concurrency (the core)** — confirm → `202` + PROCESSING + outbox `finalize` in one transaction; finalization consumer with **optimistic lock + bounded retry** → CONFIRMED/REJECTED; cancel releases a confirmed seat. *Done: unit tests for transitions and retry.*
- [x] **T8. Concurrency test** — N simultaneous confirmations for the last seat → exactly one CONFIRMED, the rest REJECTED (Testcontainers, concurrent consumers). *Done: the race test passes.*

### Phase 3 — Messaging & second context

- [x] **T9. Outbox relay & events** — relay publishes unpublished outbox rows to RabbitMQ; event envelope (`eventId`, `schemaVersion`, `occurredAt`); fanout exchange `enrollment.events`. *Done: integration test — a state change publishes exactly one event.*
- [x] **T10. notifications-service** — under `services/`, added to the Compose; own DB (`notificationsdb`); consume events → notify (logged) + append `audit_log` (idempotent by `event_id`); retry + DLQ. *Done: integration test — audits once on redelivery; poison message lands in DLQ.*

### Phase 4 — Cross-cutting

- [x] **T11. Authorization (app-side)** — resource-server JWT validation against the Keycloak realm from T2; `@PreAuthorize` per endpoint with the fine-grained rules + ownership checks. *Done: tests for `401`/`403`; student own-only enforced.*
- [x] **T12. Observability** — JSON logs with correlation/trace id across HTTP and the broker; Actuator health + Prometheus metrics. *Done: the trace id appears in logs across a request that crosses the broker.*
- [x] **T13. Frontend (Nuxt/Vuetify)** — under `web/`, added to the Compose; admin management screens + student flow (browse → cart → checkout → my enrollments with short polling); OIDC login. *Done: manual walkthrough of the approved flows; domain errors surfaced clearly.*
- [x] **T14. Full stack + scaling** — verify the complete Compose (infra from T2 + all app services) runs reproducibly; concurrency via `docker compose up --scale academic-service=2`. *Done: one `up` runs everything; the scaled run demonstrates the race.*

> Emergent in Phase 4, reconciled into the artifacts: `GET /api/students/me` + just-in-time identity linking (Student ↔ Keycloak by `keycloak_id`); catalog reads widened to students for browsing; `academic-gateway` (nginx) for a stable HTTP entry under `--scale`; outbox `trace_context` carry; and local-run fixes (RabbitMQ credentials, CORS, demo seed).

### Phase 5 — Docs & optional stretch

- [ ] **T15. README + architectural doc** — run, tests, tech, key decisions, seat protection, concurrency, events, messaging-failure handling, observability, AI use. *Done: a fresh clone runs from the README alone.*
- [ ] **T16. Stretch (optional, additive)** — Grafana/Prometheus dashboard; Traefik gateway; distributed-tracing UI; CI/CD; pagination/filter polish. *Done: each is additive; skipping never breaks the mandatory build.*
  - Delivered: optional **observability** profile (Prometheus + Grafana + Loki + Promtail) and **business** profile (Metabase over `academicdb`), both behind Compose profiles. Remaining candidates (Traefik, tracing UI, CI/CD) stay open.

## Boundaries

- Do not build: multi-tenant, payments/billing, advanced reporting beyond the audit trail, real cloud deployment.
- No realtime push (WebSocket/SSE) — short polling only.
- Do not revisit decided stack choices (RabbitMQ, optimistic locking, two services) — they are fixed by the Technical Design.
- Audit covers the enrollment lifecycle only (decided).

## Validation

- Mandatory tasks **T1–T15** complete and green before any stretch (T16).
- The **concurrency test (T8)** is the gate for "core done" — it must pass with concurrent consumers.
- The whole system must be reproducible from `docker compose up` and the README alone.

## Human Decisions

- Which stretch items (T14) to actually include.
- Specific dependency versions and any UX-writing copy not already in the UI Skeleton.
- Escalate to a human if any task surfaces a change to business rules, the data model, or an architecture decision (would loop back to the PRD/Domain Map/Technical Design).
