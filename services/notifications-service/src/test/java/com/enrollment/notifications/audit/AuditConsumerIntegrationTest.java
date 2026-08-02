package com.enrollment.notifications.audit;

import com.enrollment.notifications.AbstractIntegrationTest;
import com.enrollment.notifications.messaging.EventEnvelope;
import com.enrollment.notifications.messaging.RabbitConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AuditConsumerIntegrationTest extends AbstractIntegrationTest {

    @Autowired RabbitTemplate rabbitTemplate;
    @Autowired AuditLogRepository audit;
    @Autowired ObjectMapper om;

    @Test
    void auditsEnrollmentEventOnceIdempotently() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID enrollmentId = UUID.randomUUID();

        ObjectNode payload = om.createObjectNode();
        payload.put("enrollmentId", enrollmentId.toString());
        payload.put("studentId", UUID.randomUUID().toString());
        payload.put("classId", UUID.randomUUID().toString());
        payload.put("status", "CONFIRMED");
        EventEnvelope envelope = new EventEnvelope(
                eventId.toString(), 1, Instant.now(), "enrollment.confirmed.v1", payload);

        publish(envelope);
        awaitAuditCount(1);

        // redelivery with the same event_id -> still one row
        publish(envelope);
        Thread.sleep(500);
        assertThat(audit.count()).isEqualTo(1);

        AuditLog entry = audit.findAll().get(0);
        assertThat(entry.getEventType()).isEqualTo("enrollment.confirmed.v1");
        assertThat(entry.getEnrollmentId()).isEqualTo(enrollmentId);
        assertThat(entry.getStatus()).isEqualTo("CONFIRMED");
        assertThat(entry.getReceivedAt()).isNotNull();
    }

    private void publish(EventEnvelope envelope) {
        rabbitTemplate.convertAndSend(RabbitConfig.EVENTS_EXCHANGE, "", envelope);
    }

    private void awaitAuditCount(long expected) throws InterruptedException {
        for (int i = 0; i < 50; i++) {
            if (audit.count() == expected) {
                return;
            }
            Thread.sleep(200);
        }
        throw new AssertionError("timed out waiting for audit count " + expected);
    }
}
