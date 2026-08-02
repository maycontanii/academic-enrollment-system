-- Carry the producing request's trace context with the message, so the trace continues across the
-- broker even though publication is deferred to the outbox relay (a different thread).
ALTER TABLE outbox ADD COLUMN trace_context TEXT;
