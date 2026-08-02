# Pattern — Authorize by fine-grained action, not by coarse role

## Problem

Coarse roles (ADMIN/STUDENT) leaking into backend code couple it to the identity provider's role composition and make ownership bypasses blunt — a role check can't express "act only on your own", and a role-based bypass can over-grant.

## Approach

- The backend's vocabulary is **only fine-grained action authorities** (`adm_create_student`, `student_read_enrollment`, …). Coarse composite roles stay a Keycloak concern and are **never referenced in code**.
- Data-dependent **ownership** lives in a small guard. It **bypasses on the admin variant of the same action** (e.g. `adm_read_enrollment`), not on a coarse role; otherwise the caller is scoped to their own records (resolved by external id).
- A request with **no authentication** is treated as a trusted internal/system call, so consumers and background relays are unaffected.

## Why it's better

Decoupled (the IdP owns role composition; the backend owns action semantics) **and** safer: a `student_*` token with no linked record can never escalate to system-wide, which a coarse-role bypass would allow.

## When to use

Any resource server behind an IdP that composes roles, where some actions need per-owner scoping. Pair with [just-in-time identity linking](../architecture-decisions/0003-identity-linking-just-in-time.md).
