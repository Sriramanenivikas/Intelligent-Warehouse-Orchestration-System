CREATE SCHEMA IF NOT EXISTS notification;

CREATE TABLE notification.notifications (
    notification_id UUID PRIMARY KEY,
    external_ref_id UUID NOT NULL UNIQUE,
    shipment_id UUID NOT NULL,
    order_intent_id UUID NOT NULL,
    tracked_shipment_id UUID,
    awb_number VARCHAR(64) NOT NULL,
    audience VARCHAR(16) NOT NULL,
    channel VARCHAR(16) NOT NULL,
    template_code VARCHAR(64) NOT NULL,
    event_type VARCHAR(128) NOT NULL,
    scan_type VARCHAR(32) NOT NULL,
    status VARCHAR(16) NOT NULL,
    title VARCHAR(160) NOT NULL,
    message VARCHAR(512) NOT NULL,
    metadata_json TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    delivered_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_notifications_audience CHECK (audience IN ('CUSTOMER', 'OPS')),
    CONSTRAINT chk_notifications_channel CHECK (channel IN ('PUSH', 'SMS', 'EMAIL', 'WEBHOOK')),
    CONSTRAINT chk_notifications_status CHECK (status IN ('GENERATED', 'PUBLISHED', 'FAILED'))
);

CREATE TABLE notification.notification_outbox_events (
    outbox_event_id UUID PRIMARY KEY,
    aggregate_type VARCHAR(64) NOT NULL,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(128) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    attempts INT NOT NULL DEFAULT 0,
    last_error VARCHAR(1024),
    payload TEXT NOT NULL,
    published_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_notification_outbox_status CHECK (status IN ('PENDING', 'PUBLISHED', 'FAILED'))
);

CREATE INDEX idx_notifications_order_intent ON notification.notifications(order_intent_id, created_at);
CREATE INDEX idx_notifications_shipment ON notification.notifications(shipment_id, created_at);
CREATE INDEX idx_notifications_audience_status ON notification.notifications(audience, status, created_at);
CREATE INDEX idx_notification_outbox_status ON notification.notification_outbox_events(status, created_at);
