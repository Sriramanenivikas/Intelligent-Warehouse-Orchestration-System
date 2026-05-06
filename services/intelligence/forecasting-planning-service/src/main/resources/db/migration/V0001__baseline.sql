CREATE TABLE forecast_runs (
    forecast_run_id UUID PRIMARY KEY,
    model_version VARCHAR(128) NOT NULL,
    triggered_by VARCHAR(64) NOT NULL,
    run_status VARCHAR(32) NOT NULL,
    forecast_count INTEGER NOT NULL,
    started_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_forecast_runs_started_at ON forecast_runs (started_at DESC);

CREATE TABLE inventory_forecasts (
    forecast_id UUID PRIMARY KEY,
    forecast_run_id UUID NOT NULL REFERENCES forecast_runs (forecast_run_id) ON DELETE CASCADE,
    node_id VARCHAR(64) NOT NULL,
    sku VARCHAR(128) NOT NULL,
    current_on_hand_quantity INTEGER NOT NULL,
    current_reserved_quantity INTEGER NOT NULL,
    available_quantity INTEGER NOT NULL,
    demand_last_1h NUMERIC(18,2) NOT NULL,
    demand_last_6h NUMERIC(18,2) NOT NULL,
    demand_last_24h NUMERIC(18,2) NOT NULL,
    predicted_hourly_demand NUMERIC(18,4) NOT NULL,
    predicted_15m_demand NUMERIC(18,4) NOT NULL,
    predicted_24h_demand NUMERIC(18,4) NOT NULL,
    days_of_cover NUMERIC(18,4) NOT NULL,
    stockout_risk VARCHAR(16) NOT NULL,
    recommended_replenishment_quantity INTEGER NOT NULL,
    recommended_reorder BOOLEAN NOT NULL,
    model_version VARCHAR(128) NOT NULL,
    generated_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX uq_inventory_forecasts_run_node_sku ON inventory_forecasts (forecast_run_id, node_id, sku);
CREATE INDEX idx_inventory_forecasts_node_sku_generated_at ON inventory_forecasts (node_id, sku, generated_at DESC);
CREATE INDEX idx_inventory_forecasts_risk_generated_at ON inventory_forecasts (stockout_risk, generated_at DESC);
