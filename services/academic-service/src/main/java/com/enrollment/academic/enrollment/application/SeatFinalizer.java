package com.enrollment.academic.enrollment.application;

import com.enrollment.academic.shared.error.ErrorCodes;

import com.enrollment.academic.catalog.domain.SchoolClass;
import com.enrollment.academic.catalog.repository.SchoolClassRepository;
import com.enrollment.academic.enrollment.domain.Enrollment;
import com.enrollment.academic.enrollment.domain.EnrollmentStatus;
import com.enrollment.academic.enrollment.repository.EnrollmentRepository;
import com.enrollment.academic.shared.error.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * A single finalization attempt, in one transaction. Consuming a seat updates the
 * {@code @Version}-guarded class; a concurrent attempt that committed first makes this
 * one fail with an optimistic-lock exception at commit — retried by {@link EnrollmentFinalizer}.
 */
@Service
public class SeatFinalizer {

    private final EnrollmentRepository enrollments;
    private final SchoolClassRepository classes;

    public SeatFinalizer(EnrollmentRepository enrollments, SchoolClassRepository classes) {
        this.enrollments = enrollments;
        this.classes = classes;
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

        if (clazz.hasFreeSeat()) {
            clazz.consumeSeat();      // increments seats_used; the version check happens at commit
            enrollment.confirm();
        } else {
            enrollment.reject();
        }
        return enrollment.getStatus();
    }
}
