package com.enrollment.academic.shared.outbox;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/** A command/event written to the outbox in the same transaction as the state change. */
@Entity
@Table(name = "outbox")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "aggregate_type", nullable = false, length = 50)
    private String aggregateType;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String payload;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    /** Serialized trace context of the request that produced this message (null if untraced). */
    @Column(name = "trace_context")
    private String traceContext;

    public OutboxMessage(String aggregateType, String eventType, String payload, String traceContext) {
        this.aggregateType = aggregateType;
        this.eventType = eventType;
        this.payload = payload;
        this.traceContext = traceContext;
    }

    public void markPublished(Instant when) {
        this.publishedAt = when;
    }
}
