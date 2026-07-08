# PRD — Academic Enrollment System

## Metadata

- Status: Approved
- Tier: 2
- Mode: Product Builder
- Owner: Maycon Tanii

## Idea

A system to manage academic enrollments — students, courses, subjects and classes — where the enrollment rules are always enforced correctly, including keeping a class from being enrolled beyond its seat limit even when many students enroll at the same time.

## Product Intent

Let administrators run the academic catalog and let students enroll themselves, with a guarantee that the domain rules can never be silently broken — above all, that a class is never enrolled past its seat limit, even when many students act at the same time. The system should be reliable and its behavior clear to anyone reviewing it.

## Users

- **Administrator** — manages the academic catalog (courses, subjects, classes), student records, user access, and any enrollment.
- **Student** — browses subjects, classes and seat availability; enrolls themselves; confirms, views and cancels their own enrollments.

## Problem

Enrollment rules are easy to state and easy to break — especially the last-seat race, where two students confirm the final seat at once and the class ends up over capacity. The product must make the rules structurally safe, keep the core enrollment domain from being entangled with side concerns like notifications and auditing, and stay reliable and explainable.

## Scope

**Full scope.** Keeping seat integrity correct under simultaneous actions is the primary focus. Delivery is sequenced core-first, so the mandatory domain and its guarantees are always complete before anything else is layered on.

Business capabilities in scope:

- Manage students, courses, subjects and classes — create, edit, list, remove.
- A student browses open classes and their seat availability and enrolls themselves.
- Enroll, confirm and cancel enrollments — by the student for their own, or by an administrator for any.
- Consult enrollments by student and by class.
- Enforce every business rule below, including seat integrity when actions happen at the same time.
- **Notifications & audit:** enrollment changes can trigger notifications and are recorded for auditing, without the core enrollment flow depending on them.
- **Access control:** only authorized users can operate the system, limited by role.
- **Browsing at scale:** lists stay usable with many records (search, filtering, paging).
- Clear, usable interfaces for both administrators and students.

## Out of Scope

- Multi-tenant support for many institutions at once — a natural future evolution, not built now.
- Advanced reporting or analytics beyond the audit trail.
- Payments, billing, or academic-calendar/prerequisite logic beyond the stated rules.
- Real production hosting or scaling — the target is a correct, self-contained, reproducible solution.

## Business Rules

Each rule below is core domain behavior and a target for a critical automated test.

1. A student can only be enrolled in an **open** class.
2. A class has a **seat limit**.
3. A student **cannot have more than one active enrollment** in the same class (active = PENDING, PROCESSING or CONFIRMED).
4. An enrollment moves through statuses: **PENDING → PROCESSING → CONFIRMED or REJECTED**, and may be **CANCELLED**. (Extended beyond the brief's three statuses — see assumptions.)
5. A seat is **consumed only when an enrollment becomes CONFIRMED**; selecting (PENDING) and submitting (PROCESSING) hold no seat.
6. **Cancelling a CONFIRMED** enrollment **releases a seat**.
7. The seat rule must hold when finalizations happen **at the same time** — for the last seat, exactly one enrollment becomes CONFIRMED and the others become REJECTED.
8. Enrollments are queryable **by student** and **by class**.

### Lifecycle assumptions

Decisions taken to close gaps not spelled out in the source brief. Confirm or adjust:

- **Lifecycle (accept-then-finalize).** A student selects a class → **PENDING** (in cart, no seat). At checkout they confirm → the request is **accepted immediately** and becomes **PROCESSING** (still no seat). It is then finalized: **CONFIRMED** if a seat is secured, or **REJECTED** if the last seat was lost. The outcome becomes visible to the student shortly after, on their enrollments.
- **The last-seat race is resolved when the seat is actually taken, not by the order in which requests arrive.** When two PROCESSING enrollments compete for the final seat, exactly one becomes CONFIRMED and the other REJECTED.
- **No seat is held before CONFIRMED.** PENDING and PROCESSING reserve nothing; there is no temporary hold that expires.
- **Cancelling** is allowed from PENDING, PROCESSING or CONFIRMED, and releases a seat only if one was consumed (CONFIRMED).
- **Uniqueness is on active enrollments.** Rule 3 covers PENDING/PROCESSING/CONFIRMED; a student whose enrollment is CANCELLED or REJECTED may try again in that class.
- **Class state gates the actions.** Selecting and finalizing require the class to be open; cancelling is always allowed. Closing a class does not alter existing enrollments, but no new enrollment can be started or finalized in it.

## Authorization

Two roles (assumption — not defined in the source brief). Access is limited by role.

- **Administrator** — manages the academic catalog (courses, subjects, classes, including opening/closing classes and setting seat limits), student records, and user access. Can enroll, confirm and cancel on behalf of any student, and consult all enrollments.
- **Student** — self-service on their own enrollments only: browses classes and seat availability, enrolls themselves, confirms and cancels their own enrollments, and consults their own.

| Capability | Administrator | Student |
|---|:---:|:---:|
| Manage courses / subjects / classes | ✓ | — |
| Open/close class, set seat limit | ✓ | — |
| Manage student records & user access | ✓ | — |
| Browse classes & seat availability | ✓ | ✓ |
| Enroll / confirm / cancel | ✓ (any) | ✓ (own only) |
| Consult enrollments | ✓ (all) | ✓ (own only) |

## Non-Functional Requirements

Quality outcomes, at the business level.

- **Correctness under concurrency** — seat integrity holds when many students act simultaneously (see rule 7).
- **Exactly-once outcome** — an accepted enrollment is finalized once and only once; a transient failure is retried without ever double-consuming a seat.
- **Resilience of side effects** — failures in notifications/audit must never block, lose, or corrupt a core enrollment operation; the core succeeds independently.
- **Performance at scale** — consultations and lists stay responsive as students, classes and enrollments grow.
- **Auditability** — every enrollment state change is recorded and reviewable after the fact.
- **Security** — all access is authenticated and limited by the user's role.
- **Operability** — the system can be brought up in a consistent, self-contained way, and its behavior is visible enough to operate and troubleshoot it.
- **Usability** — domain errors are communicated clearly; a user can complete core tasks without ambiguity.

## UX Direction

- Two experiences, gated by role:
  - **Administrator:** management screens (lists + forms) for students, courses, subjects and classes, including opening/closing classes and seat limits; full enrollment consultations.
  - **Student:** self-service flow — browse open classes with seat availability → add a class (PENDING) → review and confirm (checkout, accepted immediately) → see the outcome (processing, then confirmed or rejected) on their enrollments; cancel when allowed.
- Domain errors (class closed, no seats, duplicate enrollment) surfaced as clear, user-friendly messages.
- Large lists stay comfortable to browse (search, filtering, paging).
- Fidelity is functional, not pixel-perfect: organization and error handling matter more than visual polish.

## Glossary

- **Student** — a person who can be enrolled and who self-enrolls in classes.
- **Course** — a program of study, composed of subjects.
- **Subject** — a discipline that can be offered as a class.
- **Class** — a specific offering of a subject, with a seat limit and an open/closed state, into which students enroll.
- **Enrollment** — the link between a student and a class, moving through PENDING (selected) → PROCESSING (submitted) → CONFIRMED or REJECTED, and CANCELLED when withdrawn. A seat is held only while CONFIRMED.
- **PROCESSING** — an enrollment that was submitted and accepted, awaiting its final outcome (confirmed or rejected).
- **REJECTED** — an enrollment whose finalization found no seat available (e.g., it lost the last-seat race).
- **Seat** — one unit of a class's capacity; consumed by a confirmed enrollment, released on cancellation.
- **Open class** — a class currently accepting new enrollments and confirmations.
