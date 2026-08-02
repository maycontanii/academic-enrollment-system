package com.enrollment.academic.shared.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** RabbitMQ topology: a fanout exchange for domain events, a work queue for finalize commands. */
@Configuration
public class RabbitConfig {

    public static final String EVENTS_EXCHANGE = "enrollment.events";
    public static final String FINALIZE_QUEUE = "enrollment.finalize.q";
    public static final String FINALIZE_DLQ = "enrollment.finalize.dlq";

    /** Domain events are broadcast; notifications and audit each bind their own queue (T10). */
    @Bean
    FanoutExchange enrollmentEventsExchange() {
        return new FanoutExchange(EVENTS_EXCHANGE, true, false);
    }

    @Bean
    Queue finalizeDeadLetterQueue() {
        return QueueBuilder.durable(FINALIZE_DLQ).build();
    }

    /** Finalize commands; a message that keeps failing is dead-lettered for inspection. */
    @Bean
    Queue finalizeQueue() {
        return QueueBuilder.durable(FINALIZE_QUEUE)
                .deadLetterExchange("")               // default exchange
                .deadLetterRoutingKey(FINALIZE_DLQ)
                .build();
    }

    /** JSON on the wire — Spring Boot wires this into the RabbitTemplate and the listener factory. */
    @Bean
    MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
