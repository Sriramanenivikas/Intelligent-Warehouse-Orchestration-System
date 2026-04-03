CREATE SCHEMA IF NOT EXISTS promise_allocation;

CREATE TABLE promise_allocation.node_profiles (
    node_id VARCHAR(64) PRIMARY KEY,
    node_name VARCHAR(128) NOT NULL,
    city VARCHAR(64) NOT NULL,
    state VARCHAR(64) NOT NULL,
    country VARCHAR(2) NOT NULL,
    postal_code VARCHAR(16) NOT NULL,
    priority INTEGER NOT NULL CHECK (priority > 0),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_node_profiles_active_priority
    ON promise_allocation.node_profiles (active, priority);

CREATE TABLE promise_allocation.promise_evaluations (
    evaluation_id UUID PRIMARY KEY,
    customer_id VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL,
    fulfillment_node_id VARCHAR(64),
    reason VARCHAR(512),
    delivery_address_json TEXT NOT NULL,
    requested_items_json TEXT NOT NULL,
    item_decisions_json TEXT NOT NULL,
    promised_by TIMESTAMPTZ,
    evaluated_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_promise_evaluations_status_evaluated_at
    ON promise_allocation.promise_evaluations (status, evaluated_at DESC);

INSERT INTO promise_allocation.node_profiles (
    node_id,
    node_name,
    city,
    state,
    country,
    postal_code,
    priority,
    active
) VALUES
    ('NODE-DELHI-01', 'Delhi Primary Node', 'Delhi', 'Delhi', 'IN', '110001', 1, TRUE),
    ('NODE-MUMBAI-01', 'Mumbai Secondary Node', 'Mumbai', 'Maharashtra', 'IN', '400001', 2, TRUE);
