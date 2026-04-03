ALTER TABLE order_orchestration.order_workflows
    ADD COLUMN payment_intent_id UUID,
    ADD COLUMN payment_status VARCHAR(32),
    ADD COLUMN payment_provider_reference VARCHAR(128),
    ADD COLUMN payment_failure_reason VARCHAR(512),
    ADD COLUMN payment_processed_at TIMESTAMPTZ;

CREATE INDEX idx_order_workflows_payment_status
    ON order_orchestration.order_workflows (payment_status);
