from __future__ import annotations

import math
import os
import uuid
from dataclasses import dataclass
from datetime import datetime, timezone
from typing import Any

import mlflow
import mlflow.sklearn
import numpy as np
import pandas as pd
from mlflow import MlflowClient
from sklearn.compose import ColumnTransformer
from sklearn.ensemble import RandomForestRegressor
from sklearn.impute import SimpleImputer
from sklearn.metrics import mean_absolute_error, mean_squared_error, r2_score
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import OneHotEncoder
from sqlalchemy import create_engine, text


HOURLY_HISTORY_SQL = """
WITH combos AS (
    SELECT DISTINCT node_id, sku
    FROM inventory_ledger.inventory_stock_items
    UNION
    SELECT DISTINCT node_id, sku
    FROM inventory_ledger.inventory_reservations
),
bounds AS (
    SELECT
        COALESCE(MIN(date_trunc('hour', created_at)), date_trunc('hour', now()) - interval '14 days') AS min_hour,
        date_trunc('hour', now()) AS max_hour
    FROM inventory_ledger.inventory_reservations
),
hours AS (
    SELECT generate_series(
        (SELECT min_hour FROM bounds),
        (SELECT max_hour FROM bounds),
        interval '1 hour'
    ) AS hour_bucket
),
demand AS (
    SELECT
        node_id,
        sku,
        date_trunc('hour', created_at) AS hour_bucket,
        SUM(quantity) AS demand_units
    FROM inventory_ledger.inventory_reservations
    GROUP BY 1, 2, 3
)
SELECT
    combos.node_id,
    combos.sku,
    hours.hour_bucket,
    COALESCE(demand.demand_units, 0) AS demand_units
FROM combos
CROSS JOIN hours
LEFT JOIN demand
    ON demand.node_id = combos.node_id
   AND demand.sku = combos.sku
   AND demand.hour_bucket = hours.hour_bucket
ORDER BY combos.node_id, combos.sku, hours.hour_bucket
"""

INVENTORY_SNAPSHOT_SQL = """
SELECT
    node_id,
    sku,
    on_hand_quantity,
    reserved_quantity,
    GREATEST(on_hand_quantity - reserved_quantity, 0) AS available_quantity
FROM inventory_ledger.inventory_stock_items
ORDER BY node_id, sku
"""


@dataclass
class TrainerConfig:
    postgres_dsn: str
    mlflow_tracking_uri: str
    model_name: str
    model_alias: str
    prediction_horizon_minutes: int


def load_config() -> TrainerConfig:
    return TrainerConfig(
        postgres_dsn=os.getenv(
            "FORECASTING_POSTGRES_DSN",
            "postgresql+psycopg2://iwos:iwos_secret@localhost:55432/iwos",
        ),
        mlflow_tracking_uri=os.getenv("MLFLOW_TRACKING_URI", "http://localhost:5000"),
        model_name=os.getenv("FORECASTING_MODEL_NAME", "inventory-demand-forecast"),
        model_alias=os.getenv("FORECASTING_MODEL_ALIAS", "champion"),
        prediction_horizon_minutes=int(os.getenv("FORECASTING_PREDICTION_HORIZON_MINUTES", "15")),
    )


def build_training_frame(history: pd.DataFrame, inventory: pd.DataFrame) -> pd.DataFrame:
    frame = history.copy()
    frame["hour_bucket"] = pd.to_datetime(frame["hour_bucket"], utc=True)
    frame = frame.sort_values(["node_id", "sku", "hour_bucket"]).reset_index(drop=True)

    grouped = frame.groupby(["node_id", "sku"], group_keys=False)
    frame["lag_1h"] = grouped["demand_units"].shift(1).fillna(0)
    frame["lag_2h"] = grouped["demand_units"].shift(2).fillna(0)
    frame["lag_6h"] = grouped["demand_units"].shift(6).fillna(0)
    frame["lag_24h"] = grouped["demand_units"].shift(24).fillna(0)
    frame["rolling_6h_mean"] = grouped["demand_units"].transform(
        lambda values: values.shift(1).rolling(6, min_periods=1).mean()
    ).fillna(0)
    frame["rolling_24h_mean"] = grouped["demand_units"].transform(
        lambda values: values.shift(1).rolling(24, min_periods=1).mean()
    ).fillna(0)
    frame["hour_of_day"] = frame["hour_bucket"].dt.hour
    frame["day_of_week"] = frame["hour_bucket"].dt.dayofweek

    current_inventory = inventory.rename(
        columns={
            "on_hand_quantity": "current_on_hand_quantity",
            "reserved_quantity": "current_reserved_quantity",
            "available_quantity": "available_quantity",
        }
    )
    frame = frame.merge(current_inventory, on=["node_id", "sku"], how="left")
    frame["current_on_hand_quantity"] = frame["current_on_hand_quantity"].fillna(0)
    frame["current_reserved_quantity"] = frame["current_reserved_quantity"].fillna(0)
    frame["available_quantity"] = frame["available_quantity"].fillna(0)
    return frame


def build_prediction_frame(training_frame: pd.DataFrame) -> pd.DataFrame:
    latest = training_frame.groupby(["node_id", "sku"], as_index=False).tail(1).copy()
    next_hour = latest["hour_bucket"] + pd.Timedelta(hours=1)
    latest["hour_of_day"] = next_hour.dt.hour
    latest["day_of_week"] = next_hour.dt.dayofweek
    return latest


def train_model(training_frame: pd.DataFrame) -> tuple[Pipeline, pd.DataFrame, pd.Series, pd.DataFrame, pd.Series, list[str]]:
    feature_columns = [
        "node_id",
        "sku",
        "lag_1h",
        "lag_2h",
        "lag_6h",
        "lag_24h",
        "rolling_6h_mean",
        "rolling_24h_mean",
        "hour_of_day",
        "day_of_week",
        "current_on_hand_quantity",
        "current_reserved_quantity",
        "available_quantity",
    ]

    labelled = training_frame.iloc[24:].copy()
    if labelled.empty:
        raise ValueError("Not enough historical data to train demand model")

    split_index = max(int(len(labelled) * 0.8), 1)
    if split_index >= len(labelled):
        split_index = len(labelled) - 1
    train_df = labelled.iloc[:split_index]
    validation_df = labelled.iloc[split_index:]
    if validation_df.empty:
        validation_df = labelled.iloc[-1:]
        train_df = labelled.iloc[:-1]

    x_train = train_df[feature_columns]
    y_train = train_df["demand_units"]
    x_validation = validation_df[feature_columns]
    y_validation = validation_df["demand_units"]

    numeric_features = [
        "lag_1h",
        "lag_2h",
        "lag_6h",
        "lag_24h",
        "rolling_6h_mean",
        "rolling_24h_mean",
        "hour_of_day",
        "day_of_week",
        "current_on_hand_quantity",
        "current_reserved_quantity",
        "available_quantity",
    ]
    categorical_features = ["node_id", "sku"]

    model = Pipeline(
        steps=[
            (
                "preprocessor",
                ColumnTransformer(
                    transformers=[
                        (
                            "categorical",
                            Pipeline(
                                steps=[
                                    ("imputer", SimpleImputer(strategy="most_frequent")),
                                    ("encoder", OneHotEncoder(handle_unknown="ignore")),
                                ]
                            ),
                            categorical_features,
                        ),
                        (
                            "numeric",
                            Pipeline(steps=[("imputer", SimpleImputer(strategy="constant", fill_value=0))]),
                            numeric_features,
                        ),
                    ]
                ),
            ),
            (
                "regressor",
                RandomForestRegressor(
                    n_estimators=200,
                    max_depth=12,
                    min_samples_leaf=2,
                    random_state=42,
                    n_jobs=-1,
                ),
            ),
        ]
    )
    model.fit(x_train, y_train)
    return model, x_train, y_train, x_validation, y_validation, feature_columns


def score_metrics(model: Pipeline, x_validation: pd.DataFrame, y_validation: pd.Series) -> dict[str, float]:
    predictions = np.clip(model.predict(x_validation), 0, None)
    mae = mean_absolute_error(y_validation, predictions)
    rmse = math.sqrt(mean_squared_error(y_validation, predictions))
    r2 = r2_score(y_validation, predictions) if len(y_validation) > 1 else 0.0
    return {"mae": float(mae), "rmse": float(rmse), "r2": float(r2)}


def register_model(run_id: str, config: TrainerConfig) -> tuple[str | None, str | None]:
    model_uri = f"runs:/{run_id}/model"
    registration = mlflow.register_model(model_uri=model_uri, name=config.model_name)
    client = MlflowClient(tracking_uri=config.mlflow_tracking_uri)
    client.set_registered_model_alias(config.model_name, config.model_alias, registration.version)
    return str(registration.version), f"models:/{config.model_name}@{config.model_alias}"


def stockout_risk(days_of_cover: float, available_quantity: int, predicted_hourly_demand: float) -> str:
    if available_quantity <= 0 and predicted_hourly_demand > 0:
        return "CRITICAL"
    if days_of_cover <= 0.25:
        return "HIGH"
    if days_of_cover <= 1.0:
        return "MEDIUM"
    return "LOW"


def build_forecast_rows(
    prediction_frame: pd.DataFrame,
    predictions: np.ndarray,
    forecast_run_id: str,
    model_version: str,
    generated_at: datetime,
) -> list[dict[str, Any]]:
    rows: list[dict[str, Any]] = []
    for record, predicted_hourly in zip(prediction_frame.to_dict(orient="records"), predictions, strict=True):
        predicted_hourly = max(float(predicted_hourly), 0.0)
        predicted_15m = predicted_hourly / 4.0
        predicted_24h = predicted_hourly * 24.0
        available_quantity = int(record["available_quantity"])
        days_of_cover = 999.0 if predicted_24h <= 0 else max(available_quantity, 0) / predicted_24h
        target_quantity = math.ceil(predicted_24h * (24 + 6) / 24)
        replenishment = max(0, target_quantity - available_quantity)
        rows.append(
            {
                "forecast_id": str(uuid.uuid4()),
                "forecast_run_id": forecast_run_id,
                "node_id": record["node_id"],
                "sku": record["sku"],
                "current_on_hand_quantity": int(record["current_on_hand_quantity"]),
                "current_reserved_quantity": int(record["current_reserved_quantity"]),
                "available_quantity": available_quantity,
                "demand_last_1h": round(float(record["lag_1h"]), 2),
                "demand_last_6h": round(float(record["rolling_6h_mean"]) * 6, 2),
                "demand_last_24h": round(float(record["rolling_24h_mean"]) * 24, 2),
                "predicted_hourly_demand": round(predicted_hourly, 4),
                "predicted_15m_demand": round(predicted_15m, 4),
                "predicted_24h_demand": round(predicted_24h, 4),
                "days_of_cover": round(days_of_cover, 4),
                "stockout_risk": stockout_risk(days_of_cover, available_quantity, predicted_hourly),
                "recommended_replenishment_quantity": int(replenishment),
                "recommended_reorder": replenishment > 0,
                "model_version": model_version,
                "generated_at": generated_at,
            }
        )
    return rows


def publish_results(
    engine,
    forecast_rows: list[dict[str, Any]],
    forecast_run: dict[str, Any],
    model_run: dict[str, Any],
) -> None:
    with engine.begin() as connection:
        connection.execute(
            text(
                """
                INSERT INTO forecasting_planning.forecast_runs (
                    forecast_run_id,
                    model_version,
                    triggered_by,
                    run_status,
                    forecast_count,
                    started_at,
                    completed_at
                ) VALUES (
                    :forecast_run_id,
                    :model_version,
                    :triggered_by,
                    :run_status,
                    :forecast_count,
                    :started_at,
                    :completed_at
                )
                """
            ),
            [forecast_run],
        )
        connection.execute(
            text(
                """
                INSERT INTO forecasting_planning.inventory_forecasts (
                    forecast_id,
                    forecast_run_id,
                    node_id,
                    sku,
                    current_on_hand_quantity,
                    current_reserved_quantity,
                    available_quantity,
                    demand_last_1h,
                    demand_last_6h,
                    demand_last_24h,
                    predicted_hourly_demand,
                    predicted_15m_demand,
                    predicted_24h_demand,
                    days_of_cover,
                    stockout_risk,
                    recommended_replenishment_quantity,
                    recommended_reorder,
                    model_version,
                    generated_at
                ) VALUES (
                    :forecast_id,
                    :forecast_run_id,
                    :node_id,
                    :sku,
                    :current_on_hand_quantity,
                    :current_reserved_quantity,
                    :available_quantity,
                    :demand_last_1h,
                    :demand_last_6h,
                    :demand_last_24h,
                    :predicted_hourly_demand,
                    :predicted_15m_demand,
                    :predicted_24h_demand,
                    :days_of_cover,
                    :stockout_risk,
                    :recommended_replenishment_quantity,
                    :recommended_reorder,
                    :model_version,
                    :generated_at
                )
                """
            ),
            forecast_rows,
        )
        connection.execute(
            text(
                """
                INSERT INTO forecasting_planning.forecast_model_runs (
                    model_run_id,
                    forecast_run_id,
                    mlflow_run_id,
                    registered_model_name,
                    registered_model_version,
                    model_alias,
                    algorithm,
                    training_status,
                    training_sample_count,
                    validation_sample_count,
                    feature_count,
                    prediction_horizon_minutes,
                    mae,
                    rmse,
                    r2,
                    tracking_uri,
                    artifact_uri,
                    training_started_at,
                    training_completed_at,
                    created_at
                ) VALUES (
                    :model_run_id,
                    :forecast_run_id,
                    :mlflow_run_id,
                    :registered_model_name,
                    :registered_model_version,
                    :model_alias,
                    :algorithm,
                    :training_status,
                    :training_sample_count,
                    :validation_sample_count,
                    :feature_count,
                    :prediction_horizon_minutes,
                    :mae,
                    :rmse,
                    :r2,
                    :tracking_uri,
                    :artifact_uri,
                    :training_started_at,
                    :training_completed_at,
                    :created_at
                )
                """
            ),
            [model_run],
        )


def run_training_pipeline() -> dict[str, Any]:
    config = load_config()
    mlflow.set_tracking_uri(config.mlflow_tracking_uri)
    mlflow.set_experiment("iwos-forecasting")

    engine = create_engine(config.postgres_dsn)
    history = pd.read_sql(HOURLY_HISTORY_SQL, engine)
    inventory = pd.read_sql(INVENTORY_SNAPSHOT_SQL, engine)
    training_frame = build_training_frame(history, inventory)
    prediction_frame = build_prediction_frame(training_frame)
    model, x_train, y_train, x_validation, y_validation, feature_columns = train_model(training_frame)
    metrics = score_metrics(model, x_validation, y_validation)

    training_started_at = datetime.now(timezone.utc)
    with mlflow.start_run(run_name="inventory-demand-forecast-train") as run:
        mlflow.log_params(
            {
                "algorithm": "random_forest_regressor",
                "prediction_horizon_minutes": config.prediction_horizon_minutes,
                "feature_count": len(feature_columns),
                "training_rows": len(x_train),
                "validation_rows": len(x_validation),
            }
        )
        mlflow.log_metrics(metrics)
        example_input = x_validation.head(3)
        mlflow.sklearn.log_model(model, artifact_path="model", input_example=example_input)
        registered_version, model_uri = register_model(run.info.run_id, config)
        artifact_uri = mlflow.get_artifact_uri("model")

    generated_at = datetime.now(timezone.utc)
    forecast_run_id = str(uuid.uuid4())
    model_version = model_uri or config.model_name
    forecast_rows = build_forecast_rows(
        prediction_frame=prediction_frame,
        predictions=np.clip(model.predict(prediction_frame[feature_columns]), 0, None),
        forecast_run_id=forecast_run_id,
        model_version=model_version,
        generated_at=generated_at,
    )

    forecast_run = {
        "forecast_run_id": forecast_run_id,
        "model_version": model_version,
        "triggered_by": "MLFLOW_TRAINER",
        "run_status": "COMPLETED",
        "forecast_count": len(forecast_rows),
        "started_at": training_started_at,
        "completed_at": generated_at,
    }
    model_run = {
        "model_run_id": str(uuid.uuid4()),
        "forecast_run_id": forecast_run_id,
        "mlflow_run_id": run.info.run_id,
        "registered_model_name": config.model_name,
        "registered_model_version": registered_version,
        "model_alias": config.model_alias,
        "algorithm": "random_forest_regressor",
        "training_status": "COMPLETED",
        "training_sample_count": int(len(x_train)),
        "validation_sample_count": int(len(x_validation)),
        "feature_count": len(feature_columns),
        "prediction_horizon_minutes": config.prediction_horizon_minutes,
        "mae": round(metrics["mae"], 6),
        "rmse": round(metrics["rmse"], 6),
        "r2": round(metrics["r2"], 6),
        "tracking_uri": config.mlflow_tracking_uri,
        "artifact_uri": artifact_uri,
        "training_started_at": training_started_at,
        "training_completed_at": generated_at,
        "created_at": generated_at,
    }
    publish_results(engine, forecast_rows, forecast_run, model_run)
    return {
        "forecast_run_id": forecast_run_id,
        "forecast_count": len(forecast_rows),
        "registered_model_version": registered_version,
        "metrics": metrics,
    }


if __name__ == "__main__":
    result = run_training_pipeline()
    print(result)
