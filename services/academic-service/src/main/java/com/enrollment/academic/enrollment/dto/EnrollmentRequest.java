package com.enrollment.academic.enrollment.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record EnrollmentRequest(
        @NotNull UUID studentId,
        @NotNull UUID classId) {
}
