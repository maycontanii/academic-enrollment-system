package com.enrollment.academic.catalog.repository;

import com.enrollment.academic.catalog.domain.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SubjectRepository extends JpaRepository<Subject, UUID> {
}
