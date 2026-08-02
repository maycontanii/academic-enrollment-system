package com.enrollment.notifications.notify;

import com.enrollment.notifications.messaging.EventEnvelope;
import com.enrollment.notifications.messaging.RabbitConfig;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/** Produces a user-facing notification for each enrollment event. Logged in this scope. */
@Component
public class NotifyConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotifyConsumer.class);

    @RabbitListener(queues = RabbitConfig.NOTIFY_QUEUE)
    public void onEvent(EventEnvelope envelope) {
        JsonNode payload = envelope.payload();
        log.info("Notification [{}] enrollment={} student={} status={}",
                envelope.type(),
                payload.path("enrollmentId").asText(null),
                payload.path("studentId").asText(null),
                payload.path("status").asText(null));
    }
}
