package com.enrollment.academic.shared.observability;

import brave.Tracing;
import brave.propagation.ThreadLocalCurrentTraceContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.brave.bridge.BraveBaggageManager;
import io.micrometer.tracing.brave.bridge.BraveCurrentTraceContext;
import io.micrometer.tracing.brave.bridge.BravePropagator;
import io.micrometer.tracing.brave.bridge.BraveTracer;
import io.micrometer.tracing.propagation.Propagator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The outbox's trace-carry: a context captured inside a request is restored on a thread that has no
 * active span (the relay), yielding the same trace id — which is what makes the trace cross the broker.
 */
class TracePropagationTest {

    private final Tracing tracing = Tracing.newBuilder()
            .currentTraceContext(ThreadLocalCurrentTraceContext.create())
            .build();
    private final Tracer tracer = new BraveTracer(tracing.tracer(),
            new BraveCurrentTraceContext(tracing.currentTraceContext()), new BraveBaggageManager());
    private final Propagator propagator = new BravePropagator(tracing);
    private final TracePropagation tracePropagation =
            new TracePropagation(provider(tracer), provider(propagator), new ObjectMapper());

    @AfterEach
    void closeTracing() {
        tracing.close();
    }

    @Test
    void restoresTheCapturedTraceOnAnotherThread() throws Exception {
        Span request = tracer.nextSpan().name("request").start();
        String originalTraceId;
        String captured;
        try (Tracer.SpanInScope scope = tracer.withSpan(request)) {
            originalTraceId = request.context().traceId();
            captured = tracePropagation.capture();
        } finally {
            request.end();
        }
        assertThat(captured).isNotNull();

        // No active span here — as in the scheduled relay thread.
        assertThat(tracer.currentSpan()).isNull();

        String[] seen = new String[1];
        tracePropagation.continueTrace(captured, () -> seen[0] = tracer.currentSpan().context().traceId());
        assertThat(seen[0]).isEqualTo(originalTraceId);
    }

    @Test
    void runsActionWhenNoTraceWasCarried() throws Exception {
        boolean[] ran = {false};
        tracePropagation.continueTrace(null, () -> ran[0] = true);
        assertThat(ran[0]).isTrue();
    }

    @Test
    void captureReturnsNullWithoutAnActiveSpan() {
        assertThat(tracer.currentSpan()).isNull();
        assertThat(tracePropagation.capture()).isNull();
    }

    private static <T> ObjectProvider<T> provider(T instance) {
        return new ObjectProvider<>() {
            @Override public T getObject() { return instance; }
            @Override public T getObject(Object... args) { return instance; }
            @Override public T getIfAvailable() { return instance; }
            @Override public T getIfUnique() { return instance; }
        };
    }
}
