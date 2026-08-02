package com.enrollment.notifications.audit;

import com.enrollment.notifications.messaging.EventEnvelope;
import com.enrollment.notifications.messaging.RabbitConfig;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Appends one immutable row per enrollment event. Idempotent by {@code event_id}. */
@Component
public class AuditConsumer {

    private static final Logger log = LoggerFactory.getLogger(AuditConsumer.class);

    private final AuditLogRepository repository;

    public AuditConsumer(AuditLogRepository repository) {
        this.repository = repository;
    }

    @RabbitListener(queues = RabbitConfig.AUDIT_QUEUE)
    public void onEvent(EventEnvelope envelope) {
        UUID eventId = UUID.fromString(envelope.eventId());
        if (repository.existsByEventId(eventId)) {
            return; // already audited — a redelivery is a no-op
        }
        JsonNode payload = envelope.payload();
        AuditLog entry = new AuditLog(
                eventId, envelope.type(),
                uuid(payload, "enrollmentId"), uuid(payload, "studentId"), uuid(payload, "classId"),
                text(payload, "status"), envelope.occurredAt(), payload.toString());
        try {
            repository.save(entry);
        } catch (DataIntegrityViolationException duplicate) {
            log.debug("Duplicate audit event {} ignored", eventId);
        }
    }

    private static UUID uuid(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return v == null || v.isNull() ? null : UUID.fromString(v.asText());
    }

    private static String text(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return v == null || v.isNull() ? null : v.asText();
    }
}
