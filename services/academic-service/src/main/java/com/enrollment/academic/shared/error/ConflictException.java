package com.enrollment.academic.shared.error;

/** Thrown when a request conflicts with current state (e.g. duplicate). Maps to HTTP 409. */
public class ConflictException extends RuntimeException {

    private final String code;

    public ConflictException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
