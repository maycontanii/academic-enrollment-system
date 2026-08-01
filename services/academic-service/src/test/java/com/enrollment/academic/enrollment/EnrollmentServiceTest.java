package com.enrollment.academic.enrollment;

import com.enrollment.academic.catalog.domain.ClassStatus;
import com.enrollment.academic.catalog.domain.SchoolClass;
import com.enrollment.academic.catalog.repository.SchoolClassRepository;
import com.enrollment.academic.catalog.repository.StudentRepository;
import com.enrollment.academic.enrollment.application.EnrollmentService;
import com.enrollment.academic.enrollment.domain.EnrollmentStatus;
import com.enrollment.academic.enrollment.dto.EnrollmentRequest;
import com.enrollment.academic.enrollment.repository.EnrollmentRepository;
import com.enrollment.academic.shared.error.BusinessException;
import com.enrollment.academic.shared.error.ConflictException;
import com.enrollment.academic.shared.error.NotFoundException;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @Mock EnrollmentRepository enrollments;
    @Mock StudentRepository students;
    @Mock SchoolClassRepository classes;
    @InjectMocks EnrollmentService service;

    final UUID studentId = UUID.randomUUID();
    final UUID classId = UUID.randomUUID();

    @Test
    void createsPendingWhenOpenAndNotDuplicate() {
        when(students.existsById(studentId)).thenReturn(true);
        when(classes.findById(classId)).thenReturn(Optional.of(openClass()));
        when(enrollments.existsByStudentIdAndClassIdAndStatusIn(eq(studentId), eq(classId), any())).thenReturn(false);
        when(enrollments.save(any())).thenAnswer(i -> i.getArgument(0));

        var response = service.create(new EnrollmentRequest(studentId, classId));

        assertThat(response.status()).isEqualTo(EnrollmentStatus.PENDING);
        assertThat(response.studentId()).isEqualTo(studentId);
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

    private SchoolClass openClass() {
        SchoolClass c = new SchoolClass(UUID.randomUUID(), "2026.1 - A", 30);
        c.setStatus(ClassStatus.OPEN);
        return c;
    }

    private SchoolClass closedClass() {
        return new SchoolClass(UUID.randomUUID(), "2026.1 - B", 30); // CLOSED by default
    }
}
