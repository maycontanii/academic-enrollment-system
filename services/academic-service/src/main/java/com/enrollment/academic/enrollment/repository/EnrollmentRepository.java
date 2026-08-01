package com.enrollment.academic.enrollment.repository;

import com.enrollment.academic.enrollment.domain.Enrollment;
import com.enrollment.academic.enrollment.domain.EnrollmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.UUID;

public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {

    boolean existsByStudentIdAndClassIdAndStatusIn(UUID studentId, UUID classId, Collection<EnrollmentStatus> statuses);

    @Query("""
            select e from Enrollment e
            where (:studentId is null or e.studentId = :studentId)
              and (:classId is null or e.classId = :classId)
              and (:status is null or e.status = :status)
            """)
    Page<Enrollment> search(@Param("studentId") UUID studentId,
                            @Param("classId") UUID classId,
                            @Param("status") EnrollmentStatus status,
                            Pageable pageable);
}
