-- Append-only audit trail of the enrollment lifecycle. Idempotent by event_id.
CREATE TABLE audit_log (
    id            UUID PRIMARY KEY,
    event_id      UUID NOT NULL UNIQUE,
    event_type    VARCHAR(100) NOT NULL,
    enrollment_id UUID,
    student_id    UUID,
    class_id      UUID,
    status        VARCHAR(20),
    occurred_at   TIMESTAMPTZ,
    received_at   TIMESTAMPTZ NOT NULL,
    payload       JSONB NOT NULL
);

CREATE INDEX ix_audit_enrollment ON audit_log (enrollment_id);
