# forecasting-ml

Local ML training and model registry stack for inventory demand forecasting.

Components:

- `mlflow` for experiment tracking, model registry, and supervisor UI
- `forecasting-trainer` for 15-minute retraining and prediction publication

Outputs published into `forecasting_planning`:

- `forecast_runs`
- `inventory_forecasts`
- `forecast_model_runs`
