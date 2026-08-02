package com.enrollment.academic.shared.messaging;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;

/** Stable envelope around every published domain event. */
public record EventEnvelope(
        String eventId,
        int schemaVersion,
        Instant occurredAt,
        String type,
        JsonNode payload) {
}
