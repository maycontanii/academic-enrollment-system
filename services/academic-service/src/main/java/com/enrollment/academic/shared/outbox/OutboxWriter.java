package com.enrollment.academic.shared.outbox;

import com.enrollment.academic.shared.observability.TracePropagation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

/** Writes an outbox message, serializing a typed payload to JSON — no hand-built JSON at call sites. */
@Component
public class OutboxWriter {

    private final OutboxRepository repository;
    private final ObjectMapper objectMapper;
    private final TracePropagation tracePropagation;

    public OutboxWriter(OutboxRepository repository, ObjectMapper objectMapper, TracePropagation tracePropagation) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.tracePropagation = tracePropagation;
    }

    public void write(String aggregateType, String eventType, Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            repository.save(new OutboxMessage(aggregateType, eventType, json, tracePropagation.capture()));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize outbox payload for " + eventType, e);
        }
    }
}
