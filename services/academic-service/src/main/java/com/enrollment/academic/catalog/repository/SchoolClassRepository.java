package com.enrollment.academic.catalog.repository;

import com.enrollment.academic.catalog.domain.ClassStatus;
import com.enrollment.academic.catalog.domain.SchoolClass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SchoolClassRepository extends JpaRepository<SchoolClass, UUID> {

    Page<SchoolClass> findByStatus(ClassStatus status, Pageable pageable);

    Page<SchoolClass> findBySubjectId(UUID subjectId, Pageable pageable);

    Page<SchoolClass> findBySubjectIdAndStatus(UUID subjectId, ClassStatus status, Pageable pageable);
}
