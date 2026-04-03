CREATE SCHEMA IF NOT EXISTS warehouse_orchestration;

CREATE TABLE warehouse_orchestration.fulfillment_orders (
    fulfillment_order_id UUID PRIMARY KEY,
    workflow_id UUID NOT NULL UNIQUE,
    order_intent_id UUID NOT NULL UNIQUE,
    payment_intent_id UUID NOT NULL,
    customer_id VARCHAR(64) NOT NULL,
    fulfillment_node_id VARCHAR(64) NOT NULL,
    warehouse_code VARCHAR(64) NOT NULL,
    source_topic VARCHAR(128) NOT NULL,
    source_message_key VARCHAR(128) NOT NULL UNIQUE,
    source_event_type VARCHAR(128) NOT NULL,
    source_event_occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    source_event_payload TEXT NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX idx_fulfillment_orders_status_created_at
    ON warehouse_orchestration.fulfillment_orders (status, created_at);

CREATE TABLE warehouse_orchestration.fulfillment_tasks (
    fulfillment_task_id UUID PRIMARY KEY,
    fulfillment_order_id UUID NOT NULL REFERENCES warehouse_orchestration.fulfillment_orders (fulfillment_order_id),
    task_type VARCHAR(16) NOT NULL,
    status VARCHAR(16) NOT NULL,
    sequence_number INT NOT NULL,
    node_id VARCHAR(64) NOT NULL,
    task_title VARCHAR(128) NOT NULL,
    task_payload TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX idx_fulfillment_tasks_order_id
    ON warehouse_orchestration.fulfillment_tasks (fulfillment_order_id);

CREATE INDEX idx_fulfillment_tasks_status_type
    ON warehouse_orchestration.fulfillment_tasks (status, task_type);

CREATE TABLE warehouse_orchestration.warehouse_outbox_events (
    outbox_event_id UUID PRIMARY KEY,
    aggregate_type VARCHAR(64) NOT NULL,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(128) NOT NULL,
    status VARCHAR(32) NOT NULL,
    attempts INT NOT NULL DEFAULT 0,
    last_error TEXT,
    payload TEXT NOT NULL,
    published_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX idx_warehouse_outbox_status_created_at
    ON warehouse_orchestration.warehouse_outbox_events (status, created_at);
