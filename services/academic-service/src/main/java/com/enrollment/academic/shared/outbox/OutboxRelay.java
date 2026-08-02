package com.enrollment.academic.shared.outbox;

import com.enrollment.academic.enrollment.application.EnrollmentOutbox;
import com.enrollment.academic.shared.messaging.EventEnvelope;
import com.enrollment.academic.shared.messaging.RabbitConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * Publishes unpublished outbox rows to RabbitMQ, then marks them published. At-least-once:
 * on a failure the row stays unpublished and is retried next tick (consumers are idempotent).
 * The finalize command goes to a work queue; domain events are wrapped and fanned out.
 */
@Component
public class OutboxRelay {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelay.class);
    private static final int SCHEMA_VERSION = 1;

    private final OutboxRepository repository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public OutboxRelay(OutboxRepository repository, RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.repository = repository;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelayString = "${outbox.relay.delay-ms:1000}")
    public void relay() {
        List<OutboxMessage> pending = repository.findByPublishedAtIsNullOrderByCreatedAtAsc();
        for (OutboxMessage message : pending) {
            try {
                publish(message);
                message.markPublished(Instant.now());
                repository.save(message);
            } catch (Exception e) {
                log.warn("Outbox publish failed for {} ({}), will retry", message.getId(), message.getEventType(), e);
            }
        }
    }

    private void publish(OutboxMessage message) throws Exception {
        var payload = objectMapper.readTree(message.getPayload());
        if (EnrollmentOutbox.FINALIZE.equals(message.getEventType())) {
            // command -> work queue (default exchange, routing key = queue name)
            rabbitTemplate.convertAndSend("", RabbitConfig.FINALIZE_QUEUE, payload);
        } else {
            // domain event -> fanout, wrapped in the stable envelope
            EventEnvelope envelope = new EventEnvelope(
                    message.getId().toString(), SCHEMA_VERSION, message.getCreatedAt(),
                    message.getEventType(), payload);
            rabbitTemplate.convertAndSend(RabbitConfig.EVENTS_EXCHANGE, "", envelope);
        }
    }
}
