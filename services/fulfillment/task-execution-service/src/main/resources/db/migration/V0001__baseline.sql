-- Task Execution Service Schema
-- V0001__baseline.sql

CREATE SCHEMA IF NOT EXISTS task_execution;

-- Task assignments table
CREATE TABLE task_execution.task_assignments (
    task_assignment_id UUID PRIMARY KEY,
    fulfillment_task_id UUID NOT NULL UNIQUE,
    fulfillment_order_id UUID NOT NULL,
    order_intent_id UUID NOT NULL,
    task_type VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    node_id VARCHAR(64) NOT NULL,
    task_title VARCHAR(255),
    task_payload JSONB,
    worker_id VARCHAR(64),
    claimed_at TIMESTAMPTZ,
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    failure_reason VARCHAR(512),
    attempt_count INT NOT NULL DEFAULT 0,
    source_created_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version BIGINT DEFAULT 0,

    CONSTRAINT chk_task_type CHECK (task_type IN ('PICK', 'PACK')),
    CONSTRAINT chk_status CHECK (status IN ('READY', 'CLAIMED', 'IN_PROGRESS', 'COMPLETED', 'FAILED'))
);

CREATE INDEX idx_task_assignments_status ON task_execution.task_assignments(status);
CREATE INDEX idx_task_assignments_node_status ON task_execution.task_assignments(node_id, status);
CREATE INDEX idx_task_assignments_fulfillment_order ON task_execution.task_assignments(fulfillment_order_id);
CREATE INDEX idx_task_assignments_order_intent ON task_execution.task_assignments(order_intent_id);
CREATE INDEX idx_task_assignments_worker ON task_execution.task_assignments(worker_id) WHERE worker_id IS NOT NULL;

-- Outbox table for event publishing
CREATE TABLE task_execution.task_outbox_events (
    outbox_event_id UUID PRIMARY KEY,
    aggregate_type VARCHAR(64) NOT NULL,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(128) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    attempts INT NOT NULL DEFAULT 0,
    last_error VARCHAR(1024),
    payload JSONB NOT NULL,
    published_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_outbox_status CHECK (status IN ('PENDING', 'PUBLISHED', 'FAILED'))
);

CREATE INDEX idx_task_outbox_status ON task_execution.task_outbox_events(status, created_at);
CREATE INDEX idx_task_outbox_aggregate ON task_execution.task_outbox_events(aggregate_type, aggregate_id);
