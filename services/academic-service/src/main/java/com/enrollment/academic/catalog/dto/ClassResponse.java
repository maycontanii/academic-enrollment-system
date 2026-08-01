package com.enrollment.academic.catalog.dto;

import com.enrollment.academic.catalog.domain.ClassStatus;
import com.enrollment.academic.catalog.domain.SchoolClass;

import java.util.UUID;

public record ClassResponse(
        UUID id,
        UUID subjectId,
        String label,
        int seatLimit,
        int seatsUsed,
        ClassStatus status) {

    public static ClassResponse from(SchoolClass c) {
        return new ClassResponse(c.getId(), c.getSubjectId(), c.getLabel(),
                c.getSeatLimit(), c.getSeatsUsed(), c.getStatus());
    }
}
