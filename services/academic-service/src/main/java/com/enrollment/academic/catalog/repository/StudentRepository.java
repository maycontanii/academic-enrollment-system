package com.enrollment.academic.catalog.repository;

import com.enrollment.academic.catalog.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StudentRepository extends JpaRepository<Student, UUID> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, UUID id);

    Optional<Student> findByKeycloakId(String keycloakId);
}
