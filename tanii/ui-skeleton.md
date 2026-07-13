# UI Skeleton — Academic Enrollment System

## Metadata

- Status: Approved
- Tier: 2
- Mode: Product Builder
- Derived from: PRD

## Legend

```
[ Button ]      ( input field )      [ select ▾ ]      * = required
| Col | Col |   table header         ◀ 1 2 ▶          paging
top bar: app name (left) · user/role + sign out (right)
> ...           inline state / error message
```

## Screen Inventory

Shared: 1 Login.
Administrator: 2 Admin Home · 3 Students List · 4 Student Form · 5 Courses List · 6 Course Form · 7 Subjects List · 8 Subject Form · 9 Classes List · 10 Class Form · 11 Enrollments by Class · 12 Enrollments by Student · 13 Users & Access.
Student: 14 Browse & Select · 15 Cart · 16 Checkout · 17 My Enrollments.

## Screens

### Screen 1 — Login

```
+------------------------------------------+
|           Academic Enrollment            |
|                                          |
|        ( Username / email )*             |
|        ( Password )*                     |
|                                          |
|               [ Sign in ]                |
|        > Invalid credentials             |
+------------------------------------------+
```

- Actions: [Sign in] → Administrator → Screen 2; Student → Screen 14.
- States: Loading "Signing in…"; Error "Invalid credentials" / "Account has no access".
- Navigation: entry point.

### Screen 2 — Admin Home

```
+------------------------------------------+
| Academic Enrollment       admin ▾  [Out] |
+------------------------------------------+
|  Manage                                  |
|   [ Students ]  [ Courses ]  [ Subjects ]|
|   [ Classes ]                            |
|                                          |
|  Consult                                 |
|   [ Enrollments by class ]               |
|   [ Enrollments by student ]             |
|                                          |
|  Access                                  |
|   [ Users & access ]                     |
+------------------------------------------+
```

- Actions: each button → its screen (3/5/7/9/11/12/13); [Out] → Screen 1.
- Navigation: after admin login.

### Screen 3 — Students List

```
+------------------------------------------+
| Academic Enrollment       admin ▾  [Out] |
+------------------------------------------+
| Students                [ + New student ]|
| ( 🔍 name or email ..................... )|
|                                          |
|  | Name       | Email        | Document |  |
|  --------------------------------------  |
|  | Ana Silva  | ana@x.com    | 123 [✏][🗑]|
|  | Bruno Reis | bruno@x.com  | 456 [✏][🗑]|
|                                 ◀ 1 2 ▶  |
+------------------------------------------+
```

- Actions: [+ New] → Screen 4; [✏] → Screen 4 (edit); [🗑] → delete → stays.
- States: Empty "No students yet"; Loading; Error "Could not load students"; paging when many.
- Navigation: from Screen 2; to Screen 4.

### Screen 4 — Student Form

```
+------------------------------------------+
| Academic Enrollment       admin ▾  [Out] |
+------------------------------------------+
| New / Edit Student                       |
|  Name*      ( ......................... )|
|  Email*     ( ......................... )|
|  Document   ( ......................... )|
|                                          |
|           [ Save ]    [ Cancel ]         |
|  > Email already registered              |
+------------------------------------------+
```

- Actions: [Save] → Screen 3; [Cancel] → Screen 3.
- States: Error "Email already registered" / "Name is required".
- Navigation: from/to Screen 3.

### Screen 5 — Courses List

```
+------------------------------------------+
| Academic Enrollment       admin ▾  [Out] |
+------------------------------------------+
| Courses                  [ + New course ]|
| ( 🔍 search ........................... )|
|                                          |
|  | Name             | Description | Subj |
|  --------------------------------------  |
|  | Computer Science | ...         | 6 [✏][🗑]
|                                 ◀ 1 2 ▶  |
+------------------------------------------+
```

- Actions: [+ New] → Screen 6; [✏] → Screen 6; [🗑] → delete → stays.
- States: Empty "No courses yet"; Error "Could not load courses".
- Navigation: from Screen 2; to Screen 6.

### Screen 6 — Course Form

```
+------------------------------------------+
| Academic Enrollment       admin ▾  [Out] |
+------------------------------------------+
| New / Edit Course                        |
|  Name*        ( ..................... )   |
|  Description  ( ..................... )   |
|                                          |
|           [ Save ]    [ Cancel ]         |
+------------------------------------------+
```

- Actions: [Save]/[Cancel] → Screen 5.
- States: Error "Name is required".

### Screen 7 — Subjects List

```
+------------------------------------------+
| Academic Enrollment       admin ▾  [Out] |
+------------------------------------------+
| Subjects                [ + New subject ]|
| Course [ All ▾ ]                         |
|                                          |
|  | Subject     | Course             |    |
|  --------------------------------------  |
|  | Algorithms  | Computer Science   [✏][🗑]
+------------------------------------------+
```

- Actions: [+ New] → Screen 8; [✏] → Screen 8; [🗑] → delete → stays; Course filter.
- States: Empty "No subjects yet".
- Navigation: from Screen 2; to Screen 8.

### Screen 8 — Subject Form

```
+------------------------------------------+
| Academic Enrollment       admin ▾  [Out] |
+------------------------------------------+
| New / Edit Subject                       |
|  Name*        ( ..................... )   |
|  Course*      [ Computer Science  ▾ ]     |
|  Description  ( ..................... )   |
|                                          |
|           [ Save ]    [ Cancel ]         |
+------------------------------------------+
```

- Actions: [Save]/[Cancel] → Screen 7.
- States: Error "Name is required" / "Course is required".

### Screen 9 — Classes List

```
+------------------------------------------+
| Academic Enrollment       admin ▾  [Out] |
+------------------------------------------+
| Classes                  [ + New class ] |
| Subject [ All ▾ ]   Status [ All ▾ ]     |
|                                          |
|  | Class     | Subject  | Seats | Status||
|  --------------------------------------  |
|  | 2026.1-A  | Algorithms| 3/30 | Open  ||
|  |           |           |      [Close][✏][🗑]
|  | 2026.1-B  | Algorithms| 30/30| Closed||
|  |           |           |      [Open][✏][🗑]
+------------------------------------------+
```

- Actions: [+ New] → Screen 10; [✏] → Screen 10; [Open/Close] → toggle → stays; [🗑] → delete.
- States: Empty "No classes yet"; "Seats" shown as used/limit.
- Navigation: from Screen 2; to Screen 10.

### Screen 10 — Class Form

```
+------------------------------------------+
| Academic Enrollment       admin ▾  [Out] |
+------------------------------------------+
| New / Edit Class                         |
|  Subject*     [ Algorithms        ▾ ]     |
|  Label*       ( 2026.1 - A ......... )    |
|  Seat limit*  ( 30 )                       |
|  Status*      [ Closed ▾ ]  (Open/Closed) |
|                                          |
|           [ Save ]    [ Cancel ]         |
|  > Seat limit must be greater than zero   |
+------------------------------------------+
```

- Actions: [Save]/[Cancel] → Screen 9.
- States: Error "Seat limit must be greater than zero" / "Subject is required".

### Screen 11 — Enrollments by Class

```
+------------------------------------------+
| Academic Enrollment       admin ▾  [Out] |
+------------------------------------------+
| Enrollments by class                     |
|  Class* [ 2026.1 - A ▾ ]                  |
|  Seats: 3 used · 27 free · limit 30       |
|                            [ + Enroll student ]
|  | Student    | Status     | Date |      |
|  --------------------------------------  |
|  | Ana Silva  | CONFIRMED   | 07/08 [Cancel]
|  | Bruno Reis | PROCESSING  | 07/08       |
|  | Carla Melo | REJECTED    | 07/08       |
+------------------------------------------+
```

- Actions: [+ Enroll student] → student picker → creates on behalf; [Cancel] (row) → cancels on behalf → refresh.
- States: Empty "No enrollments for this class"; Loading; paging.
- Navigation: from Screen 2.

### Screen 12 — Enrollments by Student

```
+------------------------------------------+
| Academic Enrollment       admin ▾  [Out] |
+------------------------------------------+
| Enrollments by student                   |
|  Student* [ Ana Silva ▾ ]                 |
|                                          |
|  | Class    | Subject   | Status | Date ||
|  --------------------------------------  |
|  | 2026.1-A | Algorithms| CONFIRMED |[Cancel]
+------------------------------------------+
```

- Actions: [Cancel] (row) → cancels on behalf → refresh.
- States: Empty "This student has no enrollments".
- Navigation: from Screen 2.

### Screen 13 — Users & Access

```
+------------------------------------------+
| Academic Enrollment       admin ▾  [Out] |
+------------------------------------------+
| Users & access             [ + Add user ]|
| ( 🔍 search ........................... )|
|  | User        | Role          | Status ||
|  --------------------------------------  |
|  | ana@x.com   | Student       | active [▾][x]
|  | admin@x.com | Administrator | active [▾][x]
+------------------------------------------+
```

- Actions: [+ Add user] → form (username, role); [▾] change role; [x] deactivate.
- States: Error "Could not update access".
- Navigation: from Screen 2.

### Screen 14 — Browse & Select

```
+------------------------------------------+
| Academic Enrollment   student ▾ [Cart 2] |
+------------------------------------------+
| Browse classes                           |
|  Course*   [ Computer Science     ▾ ]     |
|  Subject*  [ Algorithms           ▾ ]     |
|                                          |
|  Open classes                            |
|  --------------------------------------  |
|  2026.1 - A   Seats free: 3    [ Add ]    |
|  2026.1 - B   Seats free: 0    (Full)     |
|                                          |
|  [ Go to cart ]      [ My enrollments ]  |
|  > Already enrolled in this class         |
+------------------------------------------+
```

- Actions: [Add] → creates PENDING (in cart) → stays; [Go to cart] → Screen 15; [My enrollments] → Screen 17.
- States: Empty "No open classes for this subject"; "(Full)" when seats free = 0; Error "Already have an active enrollment in this class"; Loading.
- Navigation: from Screen 1 (student); to 15/17.

### Screen 15 — Cart

```
+------------------------------------------+
| Academic Enrollment   student ▾ [Cart 2] |
+------------------------------------------+
| Cart — PENDING                           |
|  | Class    | Subject   | Status  |      |
|  --------------------------------------  |
|  | 2026.1-A | Algorithms| PENDING [Remove]
|  | 2026.2-C | Networks  | PENDING [Remove]
|  ! 2026.1-A is now full — may fail        |
|                                          |
|  [ Keep browsing ]      [ Checkout ]     |
+------------------------------------------+
```

- Actions: [Remove] → cancels PENDING → stays; [Keep browsing] → Screen 14; [Checkout] → Screen 16.
- States: Empty "Your cart is empty"; per-item warning "now full — may fail at checkout".
- Navigation: from Screen 14; to Screen 16.

### Screen 16 — Checkout (Review & Confirm)

```
+------------------------------------------+
| Academic Enrollment   student ▾          |
+------------------------------------------+
| Review & Confirm                         |
|   • 2026.1 - A   (Algorithms)            |
|   • 2026.2 - C   (Networks)              |
|                                          |
|  [ Back to cart ]        [ Confirm ]     |
|  > Submitting…                            |
|  > Request received — finalizing          |
+------------------------------------------+
```

- Actions: [Confirm] → accepted immediately; each PENDING → PROCESSING → Screen 17; [Back to cart] → Screen 15.
- States: Loading "Submitting…"; Success "Request received — finalizing"; Error "Class is closed".
- Navigation: from Screen 15; to Screen 17.

### Screen 17 — My Enrollments

```
+------------------------------------------+
| Academic Enrollment   student ▾  [Out]   |
+------------------------------------------+
| My enrollments               [ Refresh ] |
|  | Class    | Subject   | Status        ||
|  --------------------------------------  |
|  | 2026.1-A | Algorithms| PROCESSING…    |
|  | 2026.2-C | Networks  | CONFIRMED [Cancel]
|  | 2025.2-D | Databases | REJECTED       |
|                                          |
|  [ Browse more ]                         |
+------------------------------------------+
```

- Actions: [Refresh] → re-check outcomes; [Cancel] (PENDING/PROCESSING/CONFIRMED) → cancels, releases seat if CONFIRMED; [Browse more] → Screen 14.
- States: Empty "You have no enrollments yet"; PROCESSING "Finalizing…"; CONFIRMED "Seat secured"; REJECTED "No seats were available".
- Navigation: from 14/16.

## Flows

- **Student enrolls (happy path)** — 1 → 14 (Add, PENDING) → 15 → 16 (Confirm → PROCESSING) → 17 (PROCESSING → CONFIRMED). Rules 1, 5.
- **Last-seat race** — two students Confirm the final seat at the same time → both PROCESSING; on 17 one CONFIRMED, the other REJECTED. Rule 7.
- **Cancel a confirmed enrollment** — 17 → Cancel a CONFIRMED row → seat released. Rule 6.
- **Duplicate prevented** — 14 → Add a class already active → blocked. Rule 3.
- **Admin opens a class** — 1 → 2 → 9 → 10 (seat limit) → Open. Rules 1, 2.
- **Admin consults a class** — 2 → 11 → seats used/free + students by status. Rule 8.
- **Closed class blocks enrollment** — admin Closes on 9; on 14 that class is not addable. Rule 1.
