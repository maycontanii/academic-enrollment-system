package com.enrollment.notifications.audit;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/** An append-only record of one enrollment event. Never updated. */
@Entity
@Table(name = "audit_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "event_id", nullable = false, unique = true)
    private UUID eventId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "enrollment_id")
    private UUID enrollmentId;

    @Column(name = "student_id")
    private UUID studentId;

    @Column(name = "class_id")
    private UUID classId;

    @Column(length = 20)
    private String status;

    @Column(name = "occurred_at")
    private Instant occurredAt;

    @CreationTimestamp
    @Column(name = "received_at", nullable = false, updatable = false)
    private Instant receivedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String payload;

    public AuditLog(UUID eventId, String eventType, UUID enrollmentId, UUID studentId,
                    UUID classId, String status, Instant occurredAt, String payload) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.enrollmentId = enrollmentId;
        this.studentId = studentId;
        this.classId = classId;
        this.status = status;
        this.occurredAt = occurredAt;
        this.payload = payload;
    }
}
