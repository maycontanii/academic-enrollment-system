package com.enrollment.academic.catalog.dto;

import com.enrollment.academic.catalog.domain.Course;

import java.util.UUID;

public record CourseResponse(UUID id, String name, String description) {

    public static CourseResponse from(Course c) {
        return new CourseResponse(c.getId(), c.getName(), c.getDescription());
    }
}
