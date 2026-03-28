CREATE TABLE inventory_stock_items (
    stock_item_id UUID PRIMARY KEY,
    node_id VARCHAR(64) NOT NULL,
    sku VARCHAR(128) NOT NULL,
    on_hand_quantity INTEGER NOT NULL CHECK (on_hand_quantity >= 0),
    reserved_quantity INTEGER NOT NULL CHECK (reserved_quantity >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_inventory_stock_items_node_sku UNIQUE (node_id, sku),
    CONSTRAINT chk_inventory_stock_items_on_hand_vs_reserved CHECK (on_hand_quantity >= reserved_quantity)
);

CREATE INDEX idx_inventory_stock_items_node_id ON inventory_stock_items (node_id);
CREATE INDEX idx_inventory_stock_items_sku ON inventory_stock_items (sku);

CREATE TABLE inventory_reservations (
    reservation_id UUID PRIMARY KEY,
    order_reference VARCHAR(64) NOT NULL,
    node_id VARCHAR(64) NOT NULL,
    sku VARCHAR(128) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    status VARCHAR(32) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_inventory_reservations_order_reference ON inventory_reservations (order_reference);
CREATE INDEX idx_inventory_reservations_status ON inventory_reservations (status);
CREATE INDEX idx_inventory_reservations_expires_at ON inventory_reservations (expires_at);

CREATE TABLE inventory_ledger_entries (
    ledger_entry_id UUID PRIMARY KEY,
    node_id VARCHAR(64) NOT NULL,
    sku VARCHAR(128) NOT NULL,
    entry_type VARCHAR(32) NOT NULL,
    on_hand_delta INTEGER NOT NULL,
    reserved_delta INTEGER NOT NULL,
    reason VARCHAR(128),
    reference_type VARCHAR(64),
    reference_id VARCHAR(128),
    reservation_id UUID REFERENCES inventory_reservations (reservation_id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_inventory_ledger_entries_node_sku ON inventory_ledger_entries (node_id, sku);
CREATE INDEX idx_inventory_ledger_entries_reservation_id ON inventory_ledger_entries (reservation_id);
CREATE INDEX idx_inventory_ledger_entries_created_at ON inventory_ledger_entries (created_at DESC);

CREATE TABLE inventory_command_idempotency_records (
    idempotency_record_id UUID PRIMARY KEY,
    idempotency_key VARCHAR(128) NOT NULL UNIQUE,
    operation_type VARCHAR(64) NOT NULL,
    request_hash VARCHAR(64) NOT NULL,
    resource_id UUID NOT NULL,
    response_body TEXT NOT NULL,
    http_status INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_inventory_command_idempotency_operation_type ON inventory_command_idempotency_records (operation_type);

CREATE TABLE inventory_outbox_events (
    outbox_event_id UUID PRIMARY KEY,
    aggregate_type VARCHAR(64) NOT NULL,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(128) NOT NULL,
    status VARCHAR(32) NOT NULL,
    payload TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_inventory_outbox_events_status_created_at ON inventory_outbox_events (status, created_at);
