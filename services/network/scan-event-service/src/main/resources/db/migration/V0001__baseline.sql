CREATE SCHEMA IF NOT EXISTS scan_event;

CREATE TABLE scan_event.tracked_shipments (
    tracked_shipment_id UUID PRIMARY KEY,
    shipment_id UUID NOT NULL UNIQUE,
    network_shipment_id UUID,
    fulfillment_order_id UUID NOT NULL,
    order_intent_id UUID NOT NULL,
    awb_number VARCHAR(64) NOT NULL,
    carrier_code VARCHAR(32) NOT NULL,
    customer_id VARCHAR(64),
    current_status VARCHAR(32) NOT NULL,
    last_scan_type VARCHAR(32) NOT NULL,
    last_scanned_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_tracked_shipments_status
        CHECK (current_status IN ('CREATED', 'MANIFESTED', 'HUB_RECEIVED', 'SORTED', 'OUT_FOR_DELIVERY', 'DELIVERED', 'EXCEPTION')),
    CONSTRAINT chk_tracked_shipments_scan_type
        CHECK (last_scan_type IN ('CREATED', 'MANIFESTED', 'HUB_RECEIVED', 'SORTED', 'OUT_FOR_DELIVERY', 'DELIVERED', 'EXCEPTION'))
);

CREATE TABLE scan_event.normalized_scan_events (
    normalized_scan_event_id UUID PRIMARY KEY,
    tracked_shipment_id UUID NOT NULL REFERENCES scan_event.tracked_shipments(tracked_shipment_id),
    scan_event_id UUID NOT NULL UNIQUE,
    shipment_id UUID NOT NULL,
    order_intent_id UUID NOT NULL,
    awb_number VARCHAR(64) NOT NULL,
    source_event_type VARCHAR(128) NOT NULL,
    scan_type VARCHAR(32) NOT NULL,
    status_after_event VARCHAR(32) NOT NULL,
    node_id VARCHAR(64),
    facility_code VARCHAR(64),
    notes VARCHAR(512),
    occurred_at TIMESTAMPTZ NOT NULL,
    ingested_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_normalized_scan_events_scan_type
        CHECK (scan_type IN ('CREATED', 'MANIFESTED', 'HUB_RECEIVED', 'SORTED', 'OUT_FOR_DELIVERY', 'DELIVERED', 'EXCEPTION')),
    CONSTRAINT chk_normalized_scan_events_status
        CHECK (status_after_event IN ('CREATED', 'MANIFESTED', 'HUB_RECEIVED', 'SORTED', 'OUT_FOR_DELIVERY', 'DELIVERED', 'EXCEPTION'))
);

CREATE TABLE scan_event.scan_event_outbox_events (
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
    CONSTRAINT chk_scan_event_outbox_status CHECK (status IN ('PENDING', 'PUBLISHED', 'FAILED'))
);

CREATE INDEX idx_tracked_shipments_order_intent ON scan_event.tracked_shipments(order_intent_id);
CREATE INDEX idx_tracked_shipments_awb ON scan_event.tracked_shipments(awb_number);
CREATE INDEX idx_tracked_shipments_status ON scan_event.tracked_shipments(current_status);
CREATE INDEX idx_normalized_scan_events_shipment ON scan_event.normalized_scan_events(shipment_id, occurred_at);
CREATE INDEX idx_normalized_scan_events_order_intent ON scan_event.normalized_scan_events(order_intent_id, occurred_at);
CREATE INDEX idx_normalized_scan_events_awb ON scan_event.normalized_scan_events(awb_number, occurred_at);
CREATE INDEX idx_scan_event_outbox_status ON scan_event.scan_event_outbox_events(status, created_at);
