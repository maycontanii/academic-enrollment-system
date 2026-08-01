package com.enrollment.academic.domain;

/** Enrollment lifecycle status. A seat is held only while CONFIRMED. */
public enum EnrollmentStatus {
    PENDING,
    PROCESSING,
    CONFIRMED,
    REJECTED,
    CANCELLED
}
