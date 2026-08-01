package com.enrollment.academic.enrollment.repository;

import com.enrollment.academic.enrollment.domain.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {
}
