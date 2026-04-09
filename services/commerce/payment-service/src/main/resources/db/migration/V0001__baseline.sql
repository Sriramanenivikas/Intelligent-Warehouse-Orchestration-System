CREATE SCHEMA IF NOT EXISTS payment;

CREATE TABLE payment.payment_intents (
    payment_intent_id UUID PRIMARY KEY,
    order_intent_id UUID NOT NULL UNIQUE,
    order_workflow_id UUID NOT NULL,
    customer_id VARCHAR(128) NOT NULL,
    payment_mode VARCHAR(64) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    total_amount NUMERIC(19,2) NOT NULL,
    captured_amount NUMERIC(19,2) NOT NULL DEFAULT 0,
    provider_name VARCHAR(64) NOT NULL,
    provider_reference VARCHAR(128) NOT NULL UNIQUE,
    status VARCHAR(32) NOT NULL,
    failure_reason TEXT,
    authorized_at TIMESTAMP WITH TIME ZONE,
    succeeded_at TIMESTAMP WITH TIME ZONE,
    failed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX idx_payment_intents_status_created_at
    ON payment.payment_intents (status, created_at);

CREATE TABLE payment.payment_idempotency_records (
    idempotency_record_id UUID PRIMARY KEY,
    idempotency_key VARCHAR(128) NOT NULL,
    operation_type VARCHAR(64) NOT NULL,
    request_hash VARCHAR(128) NOT NULL,
    payment_intent_id UUID NOT NULL,
    http_status INT NOT NULL,
    response_body TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX idx_payment_idempotency_key_operation
    ON payment.payment_idempotency_records (idempotency_key, operation_type);

CREATE INDEX idx_payment_idempotency_payment_intent_id
    ON payment.payment_idempotency_records (payment_intent_id);

CREATE TABLE payment.payment_outbox_events (
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

CREATE INDEX idx_payment_outbox_status_created_at
    ON payment.payment_outbox_events (status, created_at);
