CREATE TABLE order_intents (
    order_intent_id UUID PRIMARY KEY,
    customer_id VARCHAR(64) NOT NULL,
    channel VARCHAR(32) NOT NULL,
    payment_mode VARCHAR(32) NOT NULL,
    currency CHAR(3) NOT NULL,
    delivery_address_json TEXT NOT NULL,
    status VARCHAR(32) NOT NULL,
    request_hash VARCHAR(64) NOT NULL,
    accepted_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_order_intents_customer_id ON order_intents (customer_id);
CREATE INDEX idx_order_intents_status ON order_intents (status);
CREATE INDEX idx_order_intents_accepted_at ON order_intents (accepted_at DESC);

CREATE TABLE order_intent_items (
    order_intent_item_id UUID PRIMARY KEY,
    order_intent_id UUID NOT NULL REFERENCES order_intents (order_intent_id) ON DELETE CASCADE,
    sku VARCHAR(128) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_order_intent_items_order_intent_id ON order_intent_items (order_intent_id);
CREATE INDEX idx_order_intent_items_sku ON order_intent_items (sku);

CREATE TABLE idempotency_records (
    idempotency_record_id UUID PRIMARY KEY,
    idempotency_key VARCHAR(128) NOT NULL UNIQUE,
    request_hash VARCHAR(64) NOT NULL,
    order_intent_id UUID NOT NULL REFERENCES order_intents (order_intent_id) ON DELETE CASCADE,
    response_body TEXT NOT NULL,
    http_status INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_idempotency_records_order_intent_id ON idempotency_records (order_intent_id);

CREATE TABLE outbox_events (
    outbox_event_id UUID PRIMARY KEY,
    aggregate_type VARCHAR(64) NOT NULL,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(128) NOT NULL,
    status VARCHAR(32) NOT NULL,
    payload TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_outbox_events_status_created_at ON outbox_events (status, created_at);
