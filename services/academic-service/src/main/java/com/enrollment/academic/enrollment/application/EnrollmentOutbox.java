package com.enrollment.academic.enrollment.application;

import com.enrollment.academic.enrollment.domain.Enrollment;
import com.enrollment.academic.enrollment.domain.EnrollmentStatus;

import java.util.UUID;

/** Outbox message types and typed payloads emitted by the Enrollment subdomain. */
public final class EnrollmentOutbox {

    private EnrollmentOutbox() {
    }

    public static final String AGGREGATE = "Enrollment";

    /** Internal command: finalize this enrollment (secure a seat). Consumed by academic-service. */
    public static final String FINALIZE = "enrollment.finalize";

    /** Domain events (versioned) fanned out to notifications/audit. */
    public static final String CREATED = "enrollment.created.v1";
    public static final String CONFIRMED = "enrollment.confirmed.v1";
    public static final String REJECTED = "enrollment.rejected.v1";
    public static final String CANCELLED = "enrollment.cancelled.v1";

    public record FinalizeCommand(UUID enrollmentId) {
    }

    public record EnrollmentEvent(UUID enrollmentId, UUID studentId, UUID classId, EnrollmentStatus status) {
        public static EnrollmentEvent of(Enrollment e) {
            return new EnrollmentEvent(e.getId(), e.getStudentId(), e.getClassId(), e.getStatus());
        }
    }
}
