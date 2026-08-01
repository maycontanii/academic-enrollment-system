package com.enrollment.academic.shared.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/** Maps exceptions to the standardized error envelope. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse onValidation(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .toList();
        return ErrorResponse.of("validation_error", ErrorCodes.VALIDATION, "Invalid request", details);
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse onNotFound(NotFoundException ex) {
        return ErrorResponse.of("not_found", ex.getCode(), ex.getMessage(), List.of());
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse onConflict(ConflictException ex) {
        return ErrorResponse.of("conflict", ex.getCode(), ex.getMessage(), List.of());
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ErrorResponse onBusiness(BusinessException ex) {
        return ErrorResponse.of("business_error", ex.getCode(), ex.getMessage(), List.of());
    }
}
