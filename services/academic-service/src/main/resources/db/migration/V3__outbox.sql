-- Transactional outbox: rows written in the same transaction as a state change,
-- later published to the broker by a relay (T9) and marked with published_at.
CREATE TABLE outbox (
    id             UUID PRIMARY KEY,
    aggregate_type VARCHAR(50) NOT NULL,
    event_type     VARCHAR(100) NOT NULL,
    payload        JSONB NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL,
    published_at   TIMESTAMPTZ
);

-- The relay reads unpublished rows in order.
CREATE INDEX ix_outbox_unpublished ON outbox (created_at) WHERE published_at IS NULL;
