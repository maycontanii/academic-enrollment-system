package com.enrollment.academic.catalog.repository;

import com.enrollment.academic.catalog.domain.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CourseRepository extends JpaRepository<Course, UUID> {
}
