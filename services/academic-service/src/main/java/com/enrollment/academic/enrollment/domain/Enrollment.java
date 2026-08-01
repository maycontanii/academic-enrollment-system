package com.enrollment.academic.enrollment.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "enrollment")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "class_id", nullable = false)
    private UUID classId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EnrollmentStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Enrollment(UUID studentId, UUID classId) {
        this.studentId = studentId;
        this.classId = classId;
        this.status = EnrollmentStatus.PENDING;
    }

    // --- lifecycle transitions ---

    public void startProcessing() {
        this.status = EnrollmentStatus.PROCESSING;
    }

    public void confirm() {
        this.status = EnrollmentStatus.CONFIRMED;
    }

    public void reject() {
        this.status = EnrollmentStatus.REJECTED;
    }

    public void cancel() {
        this.status = EnrollmentStatus.CANCELLED;
    }

    public boolean isPending() {
        return status == EnrollmentStatus.PENDING;
    }

    public boolean isProcessing() {
        return status == EnrollmentStatus.PROCESSING;
    }

    public boolean isConfirmed() {
        return status == EnrollmentStatus.CONFIRMED;
    }

    public boolean isActive() {
        return status == EnrollmentStatus.PENDING
                || status == EnrollmentStatus.PROCESSING
                || status == EnrollmentStatus.CONFIRMED;
    }
}
