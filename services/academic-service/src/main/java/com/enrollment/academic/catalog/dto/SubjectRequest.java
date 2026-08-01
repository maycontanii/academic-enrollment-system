package com.enrollment.academic.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SubjectRequest(
        @NotBlank String name,
        @NotNull UUID courseId,
        String description) {
}
