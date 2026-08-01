package com.enrollment.academic.shared.error;

/** Thrown when a domain rule is violated (e.g. class not open, no seats). Maps to HTTP 422. */
public class BusinessException extends RuntimeException {

    private final String code;

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
