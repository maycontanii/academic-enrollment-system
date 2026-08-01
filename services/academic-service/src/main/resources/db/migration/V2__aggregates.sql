-- Aggregate tables for the Academic Enrollment bounded context.

CREATE TABLE student (
    id       UUID PRIMARY KEY,
    name     VARCHAR(255) NOT NULL,
    email    VARCHAR(255) NOT NULL UNIQUE,
    document VARCHAR(255)
);

CREATE TABLE course (
    id          UUID PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(1000)
);

CREATE TABLE subject (
    id          UUID PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    course_id   UUID NOT NULL REFERENCES course (id),
    description VARCHAR(1000)
);

CREATE TABLE class (
    id         UUID PRIMARY KEY,
    subject_id UUID NOT NULL REFERENCES subject (id),
    label      VARCHAR(255) NOT NULL,
    seat_limit INTEGER NOT NULL CHECK (seat_limit > 0),
    seats_used INTEGER NOT NULL DEFAULT 0,
    status     VARCHAR(20) NOT NULL,
    version    BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE enrollment (
    id         UUID PRIMARY KEY,
    student_id UUID NOT NULL REFERENCES student (id),
    class_id   UUID NOT NULL REFERENCES class (id),
    status     VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

-- At most one active enrollment per (student, class).
CREATE UNIQUE INDEX ux_enrollment_active
    ON enrollment (student_id, class_id)
    WHERE status IN ('PENDING', 'PROCESSING', 'CONFIRMED');

CREATE INDEX ix_enrollment_student ON enrollment (student_id);
CREATE INDEX ix_enrollment_class ON enrollment (class_id);
