-- Demo seed to make the admin side testable out of the box: one course with three subjects, a few
-- classes (including a 2-seat one to exercise the last-seat race and a CLOSED one), and three
-- students to manage, enroll on behalf, and consult.
--
-- No student is linked to a Keycloak identity here: the Keycloak user id (the token's `sub`) is
-- regenerated whenever the realm is re-imported, so a hard-coded link would go stale. Linking a
-- student to a logged-in account (for the student self-service flow) is handled at runtime.
--
-- Fixed UUIDs keep this deterministic; ON CONFLICT keeps it safe to re-run against a database that
-- already holds some of this data. Integration tests truncate these tables before each test, so the
-- seed does not affect them.

-- Course
INSERT INTO course (id, name, description) VALUES
    ('c0000000-0000-0000-0000-000000000001', 'Computer Science', 'Undergraduate program')
ON CONFLICT (id) DO NOTHING;

-- Subjects
INSERT INTO subject (id, name, course_id, description) VALUES
    ('50000000-0000-0000-0000-000000000001', 'Algorithms', 'c0000000-0000-0000-0000-000000000001', 'Data structures and algorithms'),
    ('50000000-0000-0000-0000-000000000002', 'Networks',   'c0000000-0000-0000-0000-000000000001', 'Computer networks'),
    ('50000000-0000-0000-0000-000000000003', 'Databases',  'c0000000-0000-0000-0000-000000000001', 'Relational databases')
ON CONFLICT (id) DO NOTHING;

-- Classes: two open Algorithms sections (one tiny, to demo the seat race), an open Networks section,
-- and a closed Databases section.
INSERT INTO class (id, subject_id, label, seat_limit, seats_used, status, version) VALUES
    ('c1a55000-0000-0000-0000-000000000001', '50000000-0000-0000-0000-000000000001', '2026.1 - A', 30, 0, 'OPEN',   0),
    ('c1a55000-0000-0000-0000-000000000002', '50000000-0000-0000-0000-000000000001', '2026.1 - B',  2, 0, 'OPEN',   0),
    ('c1a55000-0000-0000-0000-000000000003', '50000000-0000-0000-0000-000000000002', '2026.1 - A', 25, 0, 'OPEN',   0),
    ('c1a55000-0000-0000-0000-000000000004', '50000000-0000-0000-0000-000000000003', '2025.2 - D', 30, 0, 'CLOSED', 0)
ON CONFLICT (id) DO NOTHING;

-- Students to manage / enroll on behalf / consult.
INSERT INTO student (id, name, email, document) VALUES
    ('a0000000-0000-0000-0000-000000000001', 'Ana Silva',  'ana@x.com',   '1001'),
    ('a0000000-0000-0000-0000-000000000002', 'Bruno Reis', 'bruno@x.com', '1002'),
    ('a0000000-0000-0000-0000-000000000003', 'Carla Melo', 'carla@x.com', '1003')
ON CONFLICT (email) DO NOTHING;
