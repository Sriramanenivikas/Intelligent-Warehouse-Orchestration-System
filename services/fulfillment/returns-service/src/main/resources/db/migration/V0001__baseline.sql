CREATE SCHEMA IF NOT EXISTS returns_management;

CREATE TABLE returns_management.return_requests (
    return_request_id UUID PRIMARY KEY,
    order_intent_id UUID NOT NULL,
    fulfillment_order_id UUID NOT NULL,
    shipment_id UUID,
    customer_id VARCHAR(64) NOT NULL,
    node_id VARCHAR(64) NOT NULL,
    reason_code VARCHAR(64) NOT NULL,
    reason_detail VARCHAR(512),
    status VARCHAR(32) NOT NULL,
    item_count INTEGER NOT NULL CHECK (item_count > 0),
    items_json TEXT NOT NULL,
    requested_at TIMESTAMPTZ NOT NULL,
    approved_at TIMESTAMPTZ,
    received_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_return_requests_status_requested_at
    ON returns_management.return_requests (status, requested_at DESC);

CREATE INDEX idx_return_requests_customer_requested_at
    ON returns_management.return_requests (customer_id, requested_at DESC);
