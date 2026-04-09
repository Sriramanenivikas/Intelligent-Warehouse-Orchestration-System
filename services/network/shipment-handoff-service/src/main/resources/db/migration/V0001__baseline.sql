-- Shipment Handoff Service Schema
-- V0001__baseline.sql

CREATE SCHEMA IF NOT EXISTS shipment_handoff;

-- Shipments table
CREATE TABLE shipment_handoff.shipments (
    shipment_id UUID PRIMARY KEY,
    fulfillment_order_id UUID NOT NULL UNIQUE,
    order_intent_id UUID NOT NULL,
    workflow_id UUID,
    customer_id VARCHAR(64) NOT NULL,
    awb_number VARCHAR(64),
    carrier_code VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    origin_node_id VARCHAR(64) NOT NULL,
    destination_name VARCHAR(128),
    destination_phone VARCHAR(20),
    destination_line1 VARCHAR(255),
    destination_line2 VARCHAR(255),
    destination_city VARCHAR(64),
    destination_state VARCHAR(64),
    destination_postal_code VARCHAR(16),
    destination_country VARCHAR(3),
    weight_grams INT,
    package_count INT,
    estimated_delivery_at TIMESTAMPTZ,
    manifested_at TIMESTAMPTZ,
    dispatched_at TIMESTAMPTZ,
    delivered_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version BIGINT DEFAULT 0,

    CONSTRAINT chk_carrier_code CHECK (carrier_code IN ('INTERNAL', 'DELHIVERY', 'FEDEX', 'BLUEDART', 'ECOM_EXPRESS')),
    CONSTRAINT chk_status CHECK (status IN ('CREATED', 'MANIFESTED', 'DISPATCHED', 'IN_TRANSIT', 'OUT_FOR_DELIVERY', 'DELIVERED', 'FAILED_DELIVERY', 'RETURNED'))
);

CREATE INDEX idx_shipments_order_intent ON shipment_handoff.shipments(order_intent_id);
CREATE INDEX idx_shipments_awb ON shipment_handoff.shipments(awb_number) WHERE awb_number IS NOT NULL;
CREATE INDEX idx_shipments_status ON shipment_handoff.shipments(status);
CREATE INDEX idx_shipments_origin_status ON shipment_handoff.shipments(origin_node_id, status);
CREATE INDEX idx_shipments_customer ON shipment_handoff.shipments(customer_id);

-- Outbox table for event publishing
CREATE TABLE shipment_handoff.shipment_outbox_events (
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

CREATE INDEX idx_shipment_outbox_status ON shipment_handoff.shipment_outbox_events(status, created_at);
CREATE INDEX idx_shipment_outbox_aggregate ON shipment_handoff.shipment_outbox_events(aggregate_type, aggregate_id);
