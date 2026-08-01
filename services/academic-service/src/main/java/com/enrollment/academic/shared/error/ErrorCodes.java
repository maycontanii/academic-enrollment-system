package com.enrollment.academic.shared.error;

/** Stable error codes (the {@code code} in the error envelope). One place, no magic strings. */
public final class ErrorCodes {

    private ErrorCodes() {
    }

    public static final String VALIDATION = "VALIDATION";

    public static final String STUDENT_NOT_FOUND = "student.not_found";
    public static final String STUDENT_EMAIL_DUPLICATE = "student.email.duplicate";

    public static final String COURSE_NOT_FOUND = "course.not_found";

    public static final String SUBJECT_NOT_FOUND = "subject.not_found";

    public static final String CLASS_NOT_FOUND = "class.not_found";
    public static final String CLASS_NOT_OPEN = "class.not_open";

    public static final String ENROLLMENT_NOT_FOUND = "enrollment.not_found";
    public static final String ENROLLMENT_DUPLICATE = "enrollment.duplicate";
    public static final String ENROLLMENT_NOT_PENDING = "enrollment.not_pending";
    public static final String ENROLLMENT_NOT_CANCELLABLE = "enrollment.not_cancellable";
}
