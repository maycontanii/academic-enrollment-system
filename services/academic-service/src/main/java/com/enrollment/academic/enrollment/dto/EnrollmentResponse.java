package com.enrollment.academic.enrollment.dto;

import com.enrollment.academic.enrollment.domain.Enrollment;
import com.enrollment.academic.enrollment.domain.EnrollmentStatus;

import java.time.Instant;
import java.util.UUID;

public record EnrollmentResponse(
        UUID id,
        UUID studentId,
        UUID classId,
        EnrollmentStatus status,
        Instant createdAt,
        Instant updatedAt) {

    public static EnrollmentResponse from(Enrollment e) {
        return new EnrollmentResponse(e.getId(), e.getStudentId(), e.getClassId(),
                e.getStatus(), e.getCreatedAt(), e.getUpdatedAt());
    }
}
