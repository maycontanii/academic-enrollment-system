package com.enrollment.academic.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record ClassRequest(
        @NotNull UUID subjectId,
        @NotBlank String label,
        @Positive int seatLimit) {
}
