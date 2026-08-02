package com.enrollment.notifications.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Binds two queues to the {@code enrollment.events} fanout — one for notifying, one for auditing.
 * The fanout gives each queue a copy of every event; adding a reactor is just a new binding.
 */
@Configuration
public class RabbitConfig {

    public static final String EVENTS_EXCHANGE = "enrollment.events";
    public static final String AUDIT_QUEUE = "enrollment.audit.q";
    public static final String NOTIFY_QUEUE = "enrollment.notify.q";
    public static final String AUDIT_DLQ = "enrollment.audit.dlq";
    public static final String NOTIFY_DLQ = "enrollment.notify.dlq";

    @Bean
    FanoutExchange enrollmentEventsExchange() {
        return new FanoutExchange(EVENTS_EXCHANGE, true, false);
    }

    @Bean
    Queue auditDeadLetterQueue() {
        return QueueBuilder.durable(AUDIT_DLQ).build();
    }

    @Bean
    Queue notifyDeadLetterQueue() {
        return QueueBuilder.durable(NOTIFY_DLQ).build();
    }

    @Bean
    Queue auditQueue() {
        return QueueBuilder.durable(AUDIT_QUEUE)
                .deadLetterExchange("").deadLetterRoutingKey(AUDIT_DLQ).build();
    }

    @Bean
    Queue notifyQueue() {
        return QueueBuilder.durable(NOTIFY_QUEUE)
                .deadLetterExchange("").deadLetterRoutingKey(NOTIFY_DLQ).build();
    }

    @Bean
    Binding auditBinding(Queue auditQueue, FanoutExchange enrollmentEventsExchange) {
        return BindingBuilder.bind(auditQueue).to(enrollmentEventsExchange);
    }

    @Bean
    Binding notifyBinding(Queue notifyQueue, FanoutExchange enrollmentEventsExchange) {
        return BindingBuilder.bind(notifyQueue).to(enrollmentEventsExchange);
    }

    @Bean
    MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
