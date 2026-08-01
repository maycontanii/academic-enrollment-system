package com.enrollment.academic.shared.error;

import java.util.List;

/** Standardized error envelope: {@code { "error": { type, code, message, details[] } }}. */
public record ErrorResponse(ErrorBody error) {

    public record ErrorBody(String type, String code, String message, List<String> details) {
    }

    public static ErrorResponse of(String type, String code, String message, List<String> details) {
        return new ErrorResponse(new ErrorBody(type, code, message, details));
    }
}
