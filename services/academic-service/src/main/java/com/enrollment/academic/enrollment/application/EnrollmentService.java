package com.enrollment.academic.enrollment.application;

import com.enrollment.academic.catalog.domain.ClassStatus;
import com.enrollment.academic.catalog.domain.SchoolClass;
import com.enrollment.academic.catalog.repository.SchoolClassRepository;
import com.enrollment.academic.catalog.repository.StudentRepository;
import com.enrollment.academic.enrollment.domain.Enrollment;
import com.enrollment.academic.enrollment.domain.EnrollmentStatus;
import com.enrollment.academic.enrollment.dto.EnrollmentRequest;
import com.enrollment.academic.enrollment.dto.EnrollmentResponse;
import com.enrollment.academic.enrollment.repository.EnrollmentRepository;
import com.enrollment.academic.shared.error.BusinessException;
import com.enrollment.academic.shared.error.ConflictException;
import com.enrollment.academic.shared.error.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class EnrollmentService {

    /** An active enrollment holds the student's place in a class (uniqueness applies to these). */
    private static final List<EnrollmentStatus> ACTIVE =
            List.of(EnrollmentStatus.PENDING, EnrollmentStatus.PROCESSING, EnrollmentStatus.CONFIRMED);

    private final EnrollmentRepository enrollments;
    private final StudentRepository students;
    private final SchoolClassRepository classes;

    public EnrollmentService(EnrollmentRepository enrollments,
                             StudentRepository students,
                             SchoolClassRepository classes) {
        this.enrollments = enrollments;
        this.students = students;
        this.classes = classes;
    }

    @Transactional
    public EnrollmentResponse create(EnrollmentRequest request) {
        if (!students.existsById(request.studentId())) {
            throw new NotFoundException("student.not_found", "Student not found");
        }
        SchoolClass clazz = classes.findById(request.classId())
                .orElseThrow(() -> new NotFoundException("class.not_found", "Class not found"));
        if (clazz.getStatus() != ClassStatus.OPEN) {
            throw new BusinessException("class.not_open", "Class is not open for enrollment");
        }
        if (enrollments.existsByStudentIdAndClassIdAndStatusIn(request.studentId(), request.classId(), ACTIVE)) {
            throw new ConflictException("enrollment.duplicate", "Student already has an active enrollment in this class");
        }
        Enrollment enrollment = new Enrollment(request.studentId(), request.classId());
        return EnrollmentResponse.from(enrollments.save(enrollment));
    }

    @Transactional(readOnly = true)
    public Page<EnrollmentResponse> search(UUID studentId, UUID classId, EnrollmentStatus status, Pageable pageable) {
        return enrollments.search(studentId, classId, status, pageable).map(EnrollmentResponse::from);
    }

    @Transactional(readOnly = true)
    public EnrollmentResponse get(UUID id) {
        return EnrollmentResponse.from(find(id));
    }

    private Enrollment find(UUID id) {
        return enrollments.findById(id)
                .orElseThrow(() -> new NotFoundException("enrollment.not_found", "Enrollment not found"));
    }
}
