package com.enrollment.academic.repository;

import com.enrollment.academic.domain.SchoolClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SchoolClassRepository extends JpaRepository<SchoolClass, UUID> {
}
