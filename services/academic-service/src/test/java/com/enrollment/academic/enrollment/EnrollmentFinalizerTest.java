package com.enrollment.academic.enrollment;

import com.enrollment.academic.catalog.domain.SchoolClass;
import com.enrollment.academic.enrollment.application.EnrollmentFinalizer;
import com.enrollment.academic.enrollment.application.SeatFinalizer;
import com.enrollment.academic.enrollment.domain.EnrollmentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollmentFinalizerTest {

    @Mock SeatFinalizer seatFinalizer;
    @InjectMocks EnrollmentFinalizer finalizer;

    final UUID id = UUID.randomUUID();

    @Test
    void retriesOnOptimisticConflictThenConfirms() {
        when(seatFinalizer.attempt(id))
                .thenThrow(new ObjectOptimisticLockingFailureException(SchoolClass.class, id))
                .thenReturn(EnrollmentStatus.CONFIRMED);

        assertThat(finalizer.finalizeEnrollment(id)).isEqualTo(EnrollmentStatus.CONFIRMED);
        verify(seatFinalizer, times(2)).attempt(id);
    }

    @Test
    void returnsRejectedWithoutRetry() {
        when(seatFinalizer.attempt(id)).thenReturn(EnrollmentStatus.REJECTED);

        assertThat(finalizer.finalizeEnrollment(id)).isEqualTo(EnrollmentStatus.REJECTED);
        verify(seatFinalizer, times(1)).attempt(id);
    }

    @Test
    void givesUpAfterMaxAttempts() {
        when(seatFinalizer.attempt(id))
                .thenThrow(new ObjectOptimisticLockingFailureException(SchoolClass.class, id));

        assertThatThrownBy(() -> finalizer.finalizeEnrollment(id))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
        verify(seatFinalizer, times(5)).attempt(id);
    }
}
