package com.enrollment.academic.enrollment.application;

import java.util.UUID;

/** Outbox message types and typed payloads emitted by the Enrollment subdomain. */
public final class EnrollmentOutbox {

    private EnrollmentOutbox() {
    }

    public static final String AGGREGATE = "Enrollment";

    public static final String FINALIZE = "enrollment.finalize";

    /** Command payload: finalize this enrollment (secure a seat). */
    public record FinalizeCommand(UUID enrollmentId) {
    }
}
