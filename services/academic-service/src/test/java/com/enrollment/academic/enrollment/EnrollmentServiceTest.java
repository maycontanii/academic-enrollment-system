package com.enrollment.academic.enrollment;

import com.enrollment.academic.catalog.domain.ClassStatus;
import com.enrollment.academic.catalog.domain.SchoolClass;
import com.enrollment.academic.catalog.repository.SchoolClassRepository;
import com.enrollment.academic.catalog.repository.StudentRepository;
import com.enrollment.academic.enrollment.application.EnrollmentService;
import com.enrollment.academic.enrollment.domain.Enrollment;
import com.enrollment.academic.enrollment.domain.EnrollmentStatus;
import com.enrollment.academic.enrollment.dto.EnrollmentRequest;
import com.enrollment.academic.enrollment.repository.EnrollmentRepository;
import com.enrollment.academic.shared.error.BusinessException;
import com.enrollment.academic.shared.error.ConflictException;
import com.enrollment.academic.shared.error.NotFoundException;
import com.enrollment.academic.shared.outbox.OutboxWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @Mock EnrollmentRepository enrollments;
    @Mock StudentRepository students;
    @Mock SchoolClassRepository classes;
    @Mock OutboxWriter outboxWriter;
    @InjectMocks EnrollmentService service;

    final UUID studentId = UUID.randomUUID();
    final UUID classId = UUID.randomUUID();
    final UUID enrollmentId = UUID.randomUUID();

    // --- creation rules ---

    @Test
    void createsPendingWhenOpenAndNotDuplicate() {
        when(students.existsById(studentId)).thenReturn(true);
        when(classes.findById(classId)).thenReturn(Optional.of(openClass()));
        when(enrollments.existsByStudentIdAndClassIdAndStatusIn(eq(studentId), eq(classId), any())).thenReturn(false);
        when(enrollments.save(any())).thenAnswer(i -> i.getArgument(0));

        var response = service.create(new EnrollmentRequest(studentId, classId));

        assertThat(response.status()).isEqualTo(EnrollmentStatus.PENDING);
    }

    @Test
    void rejectsClosedClass() {
        when(students.existsById(studentId)).thenReturn(true);
        when(classes.findById(classId)).thenReturn(Optional.of(closedClass()));
        assertThatThrownBy(() -> service.create(new EnrollmentRequest(studentId, classId)))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void rejectsDuplicateActiveEnrollment() {
        when(students.existsById(studentId)).thenReturn(true);
        when(classes.findById(classId)).thenReturn(Optional.of(openClass()));
        when(enrollments.existsByStudentIdAndClassIdAndStatusIn(any(), any(), any())).thenReturn(true);
        assertThatThrownBy(() -> service.create(new EnrollmentRequest(studentId, classId)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void rejectsUnknownStudent() {
        when(students.existsById(studentId)).thenReturn(false);
        assertThatThrownBy(() -> service.create(new EnrollmentRequest(studentId, classId)))
                .isInstanceOf(NotFoundException.class);
    }

    // --- confirm / cancel transitions ---

    @Test
    void confirmMovesToProcessingAndWritesOutbox() {
        when(enrollments.findById(enrollmentId)).thenReturn(Optional.of(new Enrollment(studentId, classId)));

        var response = service.confirm(enrollmentId);

        assertThat(response.status()).isEqualTo(EnrollmentStatus.PROCESSING);
        verify(outboxWriter).write(any(), any(), any());
    }

    @Test
    void confirmRejectsNonPending() {
        Enrollment processing = new Enrollment(studentId, classId);
        processing.startProcessing();
        when(enrollments.findById(enrollmentId)).thenReturn(Optional.of(processing));
        assertThatThrownBy(() -> service.confirm(enrollmentId)).isInstanceOf(BusinessException.class);
    }

    @Test
    void cancelConfirmedReleasesSeat() {
        Enrollment confirmed = new Enrollment(studentId, classId);
        confirmed.startProcessing();
        confirmed.confirm();
        when(enrollments.findById(enrollmentId)).thenReturn(Optional.of(confirmed));
        SchoolClass clazz = openClass();
        clazz.consumeSeat(); // seatsUsed = 1
        when(classes.findById(any())).thenReturn(Optional.of(clazz));

        var response = service.cancel(enrollmentId);

        assertThat(response.status()).isEqualTo(EnrollmentStatus.CANCELLED);
        assertThat(clazz.getSeatsUsed()).isZero();
    }

    @Test
    void cancelPendingDoesNotTouchSeat() {
        when(enrollments.findById(enrollmentId)).thenReturn(Optional.of(new Enrollment(studentId, classId)));

        var response = service.cancel(enrollmentId);

        assertThat(response.status()).isEqualTo(EnrollmentStatus.CANCELLED);
        verifyNoInteractions(classes);
    }

    private SchoolClass openClass() {
        SchoolClass c = new SchoolClass(UUID.randomUUID(), "2026.1 - A", 30);
        c.setStatus(ClassStatus.OPEN);
        return c;
    }

    private SchoolClass closedClass() {
        return new SchoolClass(UUID.randomUUID(), "2026.1 - B", 30);
    }
}
