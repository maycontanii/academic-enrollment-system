package com.enrollment.academic.catalog.dto;

import com.enrollment.academic.catalog.domain.Subject;

import java.util.UUID;

public record SubjectResponse(UUID id, String name, UUID courseId, String description) {

    public static SubjectResponse from(Subject s) {
        return new SubjectResponse(s.getId(), s.getName(), s.getCourseId(), s.getDescription());
    }
}
