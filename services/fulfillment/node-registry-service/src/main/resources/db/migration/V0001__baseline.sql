CREATE SCHEMA IF NOT EXISTS node_registry;

CREATE TABLE node_registry.nodes (
    node_id VARCHAR(64) PRIMARY KEY,
    node_code VARCHAR(64) NOT NULL UNIQUE,
    display_name VARCHAR(128) NOT NULL,
    node_type VARCHAR(32) NOT NULL,
    city VARCHAR(64) NOT NULL,
    state VARCHAR(64) NOT NULL,
    country VARCHAR(2) NOT NULL,
    postal_code VARCHAR(16) NOT NULL,
    timezone VARCHAR(64) NOT NULL,
    priority INTEGER NOT NULL CHECK (priority > 0),
    supports_express BOOLEAN NOT NULL DEFAULT FALSE,
    supports_parcel BOOLEAN NOT NULL DEFAULT TRUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    external_reference_id UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_nodes_type_active_priority
    ON node_registry.nodes (node_type, active, priority);

CREATE INDEX idx_nodes_city_active
    ON node_registry.nodes (city, active);

INSERT INTO node_registry.nodes (
    node_id,
    node_code,
    display_name,
    node_type,
    city,
    state,
    country,
    postal_code,
    timezone,
    priority,
    supports_express,
    supports_parcel,
    active
) VALUES
    ('NODE-DELHI-01', 'DELHI-FC-01', 'Delhi Fulfillment Center', 'FC', 'Delhi', 'Delhi', 'IN', '110001', 'Asia/Kolkata', 1, FALSE, TRUE, TRUE),
    ('NODE-BLR-DS-01', 'BLR-DS-01', 'Bengaluru Dark Store 01', 'DARK_STORE', 'Bengaluru', 'Karnataka', 'IN', '560001', 'Asia/Kolkata', 1, TRUE, TRUE, TRUE),
    ('NODE-BLR-DS-02', 'BLR-DS-02', 'Bengaluru Dark Store 02', 'DARK_STORE', 'Bengaluru', 'Karnataka', 'IN', '560034', 'Asia/Kolkata', 2, TRUE, TRUE, TRUE),
    ('NODE-BLR-FC-01', 'BLR-FC-01', 'Bengaluru Fulfillment Center', 'FC', 'Bengaluru', 'Karnataka', 'IN', '560048', 'Asia/Kolkata', 3, FALSE, TRUE, TRUE),
    ('NODE-BLR-HUB-01', 'BLR-HUB-01', 'Bengaluru Parcel Hub', 'HUB', 'Bengaluru', 'Karnataka', 'IN', '560300', 'Asia/Kolkata', 4, FALSE, TRUE, TRUE);
