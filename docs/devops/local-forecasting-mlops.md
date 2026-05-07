# Local Forecasting MLOps

This stack provides a local production-shaped ML loop for demand forecasting.

What runs:

- MLflow tracking and model registry on `http://localhost:5005`
- Python training worker that retrains every 15 minutes
- Java `forecasting-planning-service` continues to serve dashboard-ready forecast APIs from Postgres

Why this design:

- MLflow is used because it provides local tracking, lineage, model registry, and alias-based promotion. The official docs describe local serving with `mlflow models serve` and model lifecycle management via the Model Registry.
- Feast was intentionally not added in this V1. Its local provider is centered on a local file/SQLite feature-store setup. For this repo and timeline, that adds extra moving parts without improving the forecast demo path.
- MLflow server host validation is configured explicitly because MLflow 3.5+ enforces DNS rebinding protection. The trainer accesses the server through the Docker hostname `mlflow`, so that hostname must be allowed.

Source references:

- MLflow local serving: https://mlflow.org/docs/latest/ml/deployment/deploy-model-locally
- MLflow model registry: https://mlflow.org/docs/latest/ml/model-registry/
- Feast local quickstart: https://docs.feast.dev/getting-started/quickstart

How to run:

1. Start base infra first:
   - `docker compose up -d postgres`
2. Start MLflow and trainer:
   - `docker compose -f docker-compose.ml.yml up --build -d`
3. Run `forecasting-planning-service` in external ML mode:
   - `FORECASTING_PIPELINE_MODE=EXTERNAL_ML JAVA_HOME=/Users/vikas/Library/Java/JavaVirtualMachines/temurin-21.0.10/Contents/Home mvn -q -pl services/intelligence/forecasting-planning-service spring-boot:run -Dspring-boot.run.profiles=local`

Supervisor view:

- MLflow UI: `http://localhost:5005`

Ops/dashboard APIs:

- `GET /api/v1/forecasts?limit=50`
- `GET /api/v1/forecast-runs/latest`
- `GET /api/v1/model-runs/latest`

What the trainer does every 15 minutes:

1. Reads hourly reservation demand history from `inventory_ledger.inventory_reservations`
2. Builds lag and rolling-demand features per `node_id + sku`
3. Trains a `RandomForestRegressor`
4. Logs metrics and model artifacts to MLflow
5. Registers the model under `inventory-demand-forecast`
6. Promotes the newest model to alias `champion`
7. Writes the latest prediction snapshot into `forecasting_planning`

Current limitation:

- The live dataset is still small, so the model is structurally real but data quality is demo-grade.
- As more reservation history accumulates, the model quality and stability improve without changing the integration pattern.
