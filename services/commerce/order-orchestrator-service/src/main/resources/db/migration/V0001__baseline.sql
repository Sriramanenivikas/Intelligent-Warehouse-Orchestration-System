CREATE SCHEMA IF NOT EXISTS order_orchestration;

CREATE TABLE order_orchestration.order_workflows (
    workflow_id UUID PRIMARY KEY,
    order_intent_id UUID NOT NULL UNIQUE,
    source_outbox_event_id UUID NOT NULL UNIQUE,
    customer_id VARCHAR(64) NOT NULL,
    fulfillment_node_id VARCHAR(64) NOT NULL,
    status VARCHAR(64) NOT NULL,
    failure_reason VARCHAR(512),
    accepted_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_order_workflows_status_created_at
    ON order_orchestration.order_workflows (status, created_at);

CREATE TABLE order_orchestration.order_workflow_reservations (
    workflow_reservation_id UUID PRIMARY KEY,
    workflow_id UUID NOT NULL REFERENCES order_orchestration.order_workflows (workflow_id) ON DELETE CASCADE,
    order_intent_item_id UUID NOT NULL,
    inventory_reservation_id UUID NOT NULL UNIQUE,
    node_id VARCHAR(64) NOT NULL,
    sku VARCHAR(128) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_order_workflow_reservations_workflow_id
    ON order_orchestration.order_workflow_reservations (workflow_id);

CREATE INDEX idx_order_workflow_reservations_order_intent_item_id
    ON order_orchestration.order_workflow_reservations (order_intent_item_id);

CREATE TABLE order_orchestration.order_orchestrator_outbox_events (
    outbox_event_id UUID PRIMARY KEY,
    aggregate_type VARCHAR(64) NOT NULL,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(128) NOT NULL,
    status VARCHAR(32) NOT NULL,
    payload TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_order_orchestrator_outbox_status_created_at
    ON order_orchestration.order_orchestrator_outbox_events (status, created_at);
