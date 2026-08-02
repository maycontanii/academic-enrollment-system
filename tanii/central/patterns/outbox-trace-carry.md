# Pattern — Carry the trace context through the outbox

## Problem

With a transactional outbox, publishing is deferred to a scheduled **relay thread** that has no active request. The producing request's trace has already ended, so the relay would start a brand-new trace and the consumer would log an unrelated trace id — the trace **breaks exactly at the async boundary**.

## Approach

Store the trace context **with the message** and restore it at publish time:

1. **On write** (inside the request, trace active): capture the current trace context and save it in an `outbox.trace_context` column.
2. **On publish** (relay, no trace): read it back and open a scope from it before publishing, so the producer span continues the original trace.
3. **On consume**: normal broker propagation carries the same trace id onward.

Make the tracing beans optional so the code degrades to a no-op when tracing is disabled (keeps non-observability tests unaffected).

## Result

A single trace id spans HTTP → outbox → relay → broker → consumer, even though publication is asynchronous.

## When to use

Any outbox/relay (or deferred-publish) design that also wants end-to-end distributed tracing.
