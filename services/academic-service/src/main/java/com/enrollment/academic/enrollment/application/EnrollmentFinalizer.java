package com.enrollment.academic.enrollment.application;

import com.enrollment.academic.enrollment.domain.EnrollmentStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Finalizes a PROCESSING enrollment, retrying on optimistic-lock conflicts. Each attempt runs
 * in its own transaction ({@link SeatFinalizer#attempt}); on a version conflict the class is
 * re-read, so the loser of the last-seat race ends up REJECTED rather than overselling.
 */
@Service
public class EnrollmentFinalizer {

    private static final int MAX_ATTEMPTS = 5;

    private final SeatFinalizer seatFinalizer;

    public EnrollmentFinalizer(SeatFinalizer seatFinalizer) {
        this.seatFinalizer = seatFinalizer;
    }

    public EnrollmentStatus finalizeEnrollment(UUID enrollmentId) {
        int attempt = 0;
        while (true) {
            try {
                return seatFinalizer.attempt(enrollmentId);
            } catch (ObjectOptimisticLockingFailureException conflict) {
                if (++attempt >= MAX_ATTEMPTS) {
                    throw conflict; // exhausted — the caller (consumer) will retry / dead-letter
                }
                // otherwise loop: a fresh read decides confirm-or-reject against the new seat count
            }
        }
    }
}
