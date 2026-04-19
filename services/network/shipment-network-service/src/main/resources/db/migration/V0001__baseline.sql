CREATE SCHEMA IF NOT EXISTS shipment_network;

CREATE TABLE shipment_network.network_shipments (
    network_shipment_id UUID PRIMARY KEY,
    shipment_id UUID NOT NULL UNIQUE,
    fulfillment_order_id UUID NOT NULL,
    order_intent_id UUID NOT NULL,
    awb_number VARCHAR(64) NOT NULL,
    carrier_code VARCHAR(32) NOT NULL,
    customer_id VARCHAR(64),
    origin_node_id VARCHAR(64),
    current_node_id VARCHAR(64),
    current_facility_code VARCHAR(64),
    status VARCHAR(32) NOT NULL,
    last_scan_type VARCHAR(32),
    last_scanned_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_network_shipment_status CHECK (status IN ('CREATED', 'MANIFESTED', 'HUB_RECEIVED', 'SORTED', 'OUT_FOR_DELIVERY', 'DELIVERED', 'EXCEPTION'))
);

CREATE TABLE shipment_network.scan_events (
    scan_event_id UUID PRIMARY KEY,
    network_shipment_id UUID NOT NULL REFERENCES shipment_network.network_shipments(network_shipment_id),
    shipment_id UUID NOT NULL,
    awb_number VARCHAR(64) NOT NULL,
    scan_type VARCHAR(32) NOT NULL,
    node_id VARCHAR(64),
    facility_code VARCHAR(64),
    notes VARCHAR(512),
    occurred_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_scan_type CHECK (scan_type IN ('CREATED', 'MANIFESTED', 'HUB_RECEIVED', 'SORTED', 'OUT_FOR_DELIVERY', 'DELIVERED', 'EXCEPTION'))
);

CREATE TABLE shipment_network.network_outbox_events (
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
    CONSTRAINT chk_network_outbox_status CHECK (status IN ('PENDING', 'PUBLISHED', 'FAILED'))
);

CREATE INDEX idx_network_shipments_shipment_id ON shipment_network.network_shipments(shipment_id);
CREATE INDEX idx_network_shipments_order_intent ON shipment_network.network_shipments(order_intent_id);
CREATE INDEX idx_network_shipments_awb ON shipment_network.network_shipments(awb_number);
CREATE INDEX idx_network_shipments_status ON shipment_network.network_shipments(status);
CREATE INDEX idx_scan_events_shipment ON shipment_network.scan_events(shipment_id, occurred_at);
CREATE INDEX idx_scan_events_awb ON shipment_network.scan_events(awb_number, occurred_at);
CREATE INDEX idx_network_outbox_status ON shipment_network.network_outbox_events(status, created_at);
