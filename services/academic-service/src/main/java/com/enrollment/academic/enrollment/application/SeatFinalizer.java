package com.enrollment.academic.enrollment.application;

import com.enrollment.academic.catalog.domain.SchoolClass;
import com.enrollment.academic.catalog.repository.SchoolClassRepository;
import com.enrollment.academic.enrollment.domain.Enrollment;
import com.enrollment.academic.enrollment.domain.EnrollmentStatus;
import com.enrollment.academic.enrollment.repository.EnrollmentRepository;
import com.enrollment.academic.shared.error.ErrorCodes;
import com.enrollment.academic.shared.error.NotFoundException;
import com.enrollment.academic.shared.outbox.OutboxWriter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * A single finalization attempt, in one transaction. Consuming a seat updates the
 * {@code @Version}-guarded class; a concurrent attempt that committed first makes this
 * one fail with an optimistic-lock exception at commit — retried by {@link EnrollmentFinalizer}.
 * The outcome event is written to the outbox in the same transaction (rolled back on conflict).
 */
@Service
public class SeatFinalizer {

    private final EnrollmentRepository enrollments;
    private final SchoolClassRepository classes;
    private final OutboxWriter outboxWriter;

    public SeatFinalizer(EnrollmentRepository enrollments, SchoolClassRepository classes, OutboxWriter outboxWriter) {
        this.enrollments = enrollments;
        this.classes = classes;
        this.outboxWriter = outboxWriter;
    }

    @Transactional
    public EnrollmentStatus attempt(UUID enrollmentId) {
        Enrollment enrollment = enrollments.findById(enrollmentId)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.ENROLLMENT_NOT_FOUND, "Enrollment not found"));

        // Idempotent: a redelivered command for an already-finalized enrollment is a no-op.
        if (!enrollment.isProcessing()) {
            return enrollment.getStatus();
        }

        SchoolClass clazz = classes.findById(enrollment.getClassId())
                .orElseThrow(() -> new NotFoundException(ErrorCodes.CLASS_NOT_FOUND, "Class not found"));

        String eventType;
        if (clazz.hasFreeSeat()) {
            clazz.consumeSeat();      // increments seats_used; the version check happens at commit
            enrollment.confirm();
            eventType = EnrollmentOutbox.CONFIRMED;
        } else {
            enrollment.reject();
            eventType = EnrollmentOutbox.REJECTED;
        }
        outboxWriter.write(EnrollmentOutbox.AGGREGATE, eventType, EnrollmentOutbox.EnrollmentEvent.of(enrollment));
        return enrollment.getStatus();
    }
}
