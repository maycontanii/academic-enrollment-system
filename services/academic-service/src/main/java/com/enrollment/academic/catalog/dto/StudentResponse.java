package com.enrollment.academic.catalog.dto;

import com.enrollment.academic.catalog.domain.Student;

import java.util.UUID;

public record StudentResponse(UUID id, String name, String email, String document) {

    public static StudentResponse from(Student s) {
        return new StudentResponse(s.getId(), s.getName(), s.getEmail(), s.getDocument());
    }
}
