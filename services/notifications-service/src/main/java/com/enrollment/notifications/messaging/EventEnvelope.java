package com.enrollment.notifications.messaging;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;

/** Local copy of the published-language envelope emitted by academic-service. */
public record EventEnvelope(
        String eventId,
        int schemaVersion,
        Instant occurredAt,
        String type,
        JsonNode payload) {
}
