package com.enrollment.academic.catalog.dto;

import com.enrollment.academic.catalog.domain.Student;

import java.util.UUID;

/** {@code linked} tells the UI whether this student is tied to a Keycloak login (self-service). */
public record StudentResponse(UUID id, String name, String email, String document, boolean linked) {

    public static StudentResponse from(Student s) {
        return new StudentResponse(s.getId(), s.getName(), s.getEmail(), s.getDocument(), s.getKeycloakId() != null);
    }
}
