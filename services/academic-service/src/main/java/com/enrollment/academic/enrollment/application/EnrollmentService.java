package com.enrollment.academic.enrollment.application;

import com.enrollment.academic.shared.error.ErrorCodes;

import com.enrollment.academic.catalog.domain.ClassStatus;
import com.enrollment.academic.catalog.domain.SchoolClass;
import com.enrollment.academic.catalog.repository.SchoolClassRepository;
import com.enrollment.academic.catalog.repository.StudentRepository;
import com.enrollment.academic.enrollment.domain.Enrollment;
import com.enrollment.academic.enrollment.domain.EnrollmentStatus;
import com.enrollment.academic.enrollment.dto.EnrollmentRequest;
import com.enrollment.academic.enrollment.dto.EnrollmentResponse;
import com.enrollment.academic.enrollment.repository.EnrollmentRepository;
import com.enrollment.academic.shared.outbox.OutboxWriter;
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
    private final OutboxWriter outboxWriter;

    public EnrollmentService(EnrollmentRepository enrollments,
                             StudentRepository students,
                             SchoolClassRepository classes,
                             OutboxWriter outboxWriter) {
        this.enrollments = enrollments;
        this.students = students;
        this.classes = classes;
        this.outboxWriter = outboxWriter;
    }

    @Transactional
    public EnrollmentResponse create(EnrollmentRequest request) {
        if (!students.existsById(request.studentId())) {
            throw new NotFoundException(ErrorCodes.STUDENT_NOT_FOUND, "Student not found");
        }
        SchoolClass clazz = classes.findById(request.classId())
                .orElseThrow(() -> new NotFoundException(ErrorCodes.CLASS_NOT_FOUND, "Class not found"));
        if (clazz.getStatus() != ClassStatus.OPEN) {
            throw new BusinessException(ErrorCodes.CLASS_NOT_OPEN, "Class is not open for enrollment");
        }
        if (enrollments.existsByStudentIdAndClassIdAndStatusIn(request.studentId(), request.classId(), ACTIVE)) {
            throw new ConflictException(ErrorCodes.ENROLLMENT_DUPLICATE, "Student already has an active enrollment in this class");
        }
        Enrollment enrollment = enrollments.save(new Enrollment(request.studentId(), request.classId()));
        outboxWriter.write(EnrollmentOutbox.AGGREGATE, EnrollmentOutbox.CREATED,
                EnrollmentOutbox.EnrollmentEvent.of(enrollment));
        return EnrollmentResponse.from(enrollment);
    }

    /** Accept-then-finalize: move to PROCESSING and enqueue a finalize command via the outbox. */
    @Transactional
    public EnrollmentResponse confirm(UUID id) {
        Enrollment enrollment = find(id);
        if (!enrollment.isPending()) {
            throw new BusinessException(ErrorCodes.ENROLLMENT_NOT_PENDING, "Only a pending enrollment can be confirmed");
        }
        enrollment.startProcessing();
        outboxWriter.write(EnrollmentOutbox.AGGREGATE, EnrollmentOutbox.FINALIZE,
                new EnrollmentOutbox.FinalizeCommand(id));
        return EnrollmentResponse.from(enrollment);
    }

    @Transactional
    public EnrollmentResponse cancel(UUID id) {
        Enrollment enrollment = find(id);
        if (!enrollment.isActive()) {
            throw new BusinessException(ErrorCodes.ENROLLMENT_NOT_CANCELLABLE, "Enrollment cannot be cancelled");
        }
        if (enrollment.isConfirmed()) {
            SchoolClass clazz = classes.findById(enrollment.getClassId())
                    .orElseThrow(() -> new NotFoundException(ErrorCodes.CLASS_NOT_FOUND, "Class not found"));
            clazz.releaseSeat();
        }
        enrollment.cancel();
        outboxWriter.write(EnrollmentOutbox.AGGREGATE, EnrollmentOutbox.CANCELLED,
                EnrollmentOutbox.EnrollmentEvent.of(enrollment));
        return EnrollmentResponse.from(enrollment);
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
                .orElseThrow(() -> new NotFoundException(ErrorCodes.ENROLLMENT_NOT_FOUND, "Enrollment not found"));
    }
}
