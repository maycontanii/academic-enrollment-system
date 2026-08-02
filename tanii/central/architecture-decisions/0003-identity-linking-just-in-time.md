# ADR-0003 — Identity linking: just-in-time, IdP owns identity

Status: Accepted

## Context

There are two distinct concepts: a **Student** (academic record the app owns) and a **login identity** (owned by Keycloak). Something has to connect them so a student can self-serve — without the app becoming an identity manager.

## Decision

Keycloak owns identities; the app owns the `Student` record; they are linked by `student.keycloak_id`. The app **does not provision Keycloak users**. On a student's **first authenticated call** (`GET /api/students/me`), the app links an existing record by the token's `email` or materializes one from the token. Admins create logins in the Keycloak console (a per-student shortcut in the UI points there).

## Rationale

- No Keycloak admin credentials embedded in the app; the IdP stays the source of truth for accounts and roles.
- The token `sub` is regenerated when the realm is re-imported, so the link is **never hard-coded** — it is resolved at runtime.
- Self-service works with no manual linking step.

## Consequences

- `students/me` performs a write on first login (link or create); it must be idempotent and safe for repeat calls.
- Only a caller with the student role is ever auto-provisioned; an admin without a linked record gets a 404 rather than an accidental student row.

## Related

- Enforcement: [authorize-by-action-not-role](../patterns/authorize-by-action-not-role.md).
