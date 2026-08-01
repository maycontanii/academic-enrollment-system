package com.enrollment.academic.catalog.dto;

import jakarta.validation.constraints.NotBlank;

public record CourseRequest(
        @NotBlank String name,
        String description) {
}
