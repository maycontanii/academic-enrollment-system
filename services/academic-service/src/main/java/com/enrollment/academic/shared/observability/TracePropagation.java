package com.enrollment.academic.shared.observability;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Bridges tracing across the outbox: publication is deferred to a scheduled relay thread that has no
 * active span, so the producing request's trace context is captured at write time and restored at
 * publish time. Then the producer/consumer observations continue that same trace across the broker.
 *
 * <p>The tracing beans are optional (absent in slice tests without {@code @AutoConfigureObservability}),
 * so both operations degrade to no-ops rather than requiring them.
 */
@Component
public class TracePropagation {

    private static final TypeReference<Map<String, String>> CARRIER = new TypeReference<>() {
    };

    private final ObjectProvider<Tracer> tracerProvider;
    private final ObjectProvider<Propagator> propagatorProvider;
    private final ObjectMapper objectMapper;

    public TracePropagation(ObjectProvider<Tracer> tracerProvider,
                            ObjectProvider<Propagator> propagatorProvider,
                            ObjectMapper objectMapper) {
        this.tracerProvider = tracerProvider;
        this.propagatorProvider = propagatorProvider;
        this.objectMapper = objectMapper;
    }

    /** Serializes the current trace context, or returns null when there is nothing to carry. */
    public String capture() {
        Tracer tracer = tracerProvider.getIfAvailable();
        Propagator propagator = propagatorProvider.getIfAvailable();
        if (tracer == null || propagator == null || tracer.currentSpan() == null) {
            return null;
        }
        Map<String, String> carrier = new HashMap<>();
        propagator.inject(tracer.currentSpan().context(), carrier, Map::put);
        try {
            return objectMapper.writeValueAsString(carrier);
        } catch (Exception e) {
            return null; // carrying the trace is best-effort; never fail the publish over it
        }
    }

    /** Runs {@code action} inside the carried trace, so its spans continue the original request. */
    public void continueTrace(String traceContext, TraceScopedAction action) throws Exception {
        Tracer tracer = tracerProvider.getIfAvailable();
        Propagator propagator = propagatorProvider.getIfAvailable();
        if (traceContext == null || tracer == null || propagator == null) {
            action.run();
            return;
        }
        Map<String, String> carrier = objectMapper.readValue(traceContext, CARRIER);
        Span span = propagator.extract(carrier, Map::get).name("outbox.publish").start();
        try (Tracer.SpanInScope ignored = tracer.withSpan(span)) {
            action.run();
        } finally {
            span.end();
        }
    }

    /** An action that may throw — the outbox relay's publish is checked. */
    @FunctionalInterface
    public interface TraceScopedAction {
        void run() throws Exception;
    }
}
