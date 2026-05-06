CREATE SCHEMA IF NOT EXISTS control_tower;

CREATE TABLE control_tower.control_tower_snapshots (
    control_tower_snapshot_id UUID PRIMARY KEY,
    snapshot_type VARCHAR(32) NOT NULL,
    model_version VARCHAR(128) NOT NULL,
    payload_json TEXT NOT NULL,
    generated_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_control_tower_snapshots_generated_at ON control_tower.control_tower_snapshots (generated_at DESC);
