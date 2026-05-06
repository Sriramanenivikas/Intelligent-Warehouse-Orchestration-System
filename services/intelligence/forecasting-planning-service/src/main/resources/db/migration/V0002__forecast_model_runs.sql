CREATE TABLE forecasting_planning.forecast_model_runs (
    model_run_id UUID PRIMARY KEY,
    forecast_run_id UUID NOT NULL REFERENCES forecasting_planning.forecast_runs (forecast_run_id) ON DELETE CASCADE,
    mlflow_run_id VARCHAR(64),
    registered_model_name VARCHAR(128) NOT NULL,
    registered_model_version VARCHAR(32),
    model_alias VARCHAR(32),
    algorithm VARCHAR(64) NOT NULL,
    training_status VARCHAR(32) NOT NULL,
    training_sample_count INTEGER NOT NULL,
    validation_sample_count INTEGER NOT NULL,
    feature_count INTEGER NOT NULL,
    prediction_horizon_minutes INTEGER NOT NULL,
    mae NUMERIC(18,6),
    rmse NUMERIC(18,6),
    r2 NUMERIC(18,6),
    tracking_uri VARCHAR(256),
    artifact_uri VARCHAR(512),
    training_started_at TIMESTAMPTZ NOT NULL,
    training_completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_forecast_model_runs_completed_at
    ON forecasting_planning.forecast_model_runs (training_completed_at DESC, created_at DESC);
