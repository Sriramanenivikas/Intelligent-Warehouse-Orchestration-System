# ML Ops Pipeline - Forecasting & Planning

## Overview

The IWOS ML ops pipeline provides continuous model training, registry management, and inference serving for inventory demand forecasting and replenishment planning. It uses **MLflow** for experiment tracking, model registry, and local serving, with a Python trainer worker that retrains every 15 minutes and a Java forecasting service that exposes predictions to the dashboard.

---

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│  Demand Signal Sources (Postgres)                               │
│  - inventory_ledger.inventory_reservations                      │
│  - inventory_ledger.inventory_stock_items                       │
└────────────────┬────────────────────────────────────────────────┘
                 │
                 │ (reads demand history every 15 min)
                 ▼
┌─────────────────────────────────────────────────────────────────┐
│  Python Trainer Worker (Docker)                                 │
│  - IWOS/ml/forecasting/src/train_and_publish.py                │
│  - Feature engineering from real reservation/stock data         │
│  - Model training: sklearn RandomForest / XGBoost               │
│  - Logs experiments to MLflow tracking server                   │
│  - Publishes best model to MLflow registry with "champion" alias│
└────────────────┬────────────────────────────────────────────────┘
                 │
                 │ (model metadata + snapshot)
                 ▼
┌─────────────────────────────────────────────────────────────────┐
│  MLflow Registry & Tracking (localhost:5005)                    │
│  - Experiment tracking: metrics, params, model versions         │
│  - Model registry: inventory-demand-forecast                    │
│  - Alias management: champion, staging, ...                     │
│  - Model artifacts stored locally                               │
└────────────────┬────────────────────────────────────────────────┘
                 │
                 │ (latest champion model + snapshot)
                 ▼
┌─────────────────────────────────────────────────────────────────┐
│  Forecasting Service (Java Spring Boot on 8093)                 │
│  - EXTERNAL_ML mode: reads model runs, does not train           │
│  - GET /api/v1/model-runs/latest                                │
│  - GET /api/v1/forecast-runs/latest                             │
│  - GET /api/v1/forecasts?limit=...                              │
└────────────────┬────────────────────────────────────────────────┘
                 │
                 │ (clean forecast data)
                 ▼
┌─────────────────────────────────────────────────────────────────┐
│  Dashboard / Ops APIs                                           │
│  - Stock prediction data (15-min refresh)                       │
│  - Risk levels & replenishment recommendations                  │
│  - Model training metrics for supervisors                       │
└─────────────────────────────────────────────────────────────────┘
```

---

## Components

### 1. Python Trainer (`IWOS/ml/forecasting`)

**Purpose**: Retrains demand forecast model every 15 minutes, publishes snapshot to Postgres, registers model in MLflow.

**Key Files**:
- `src/train_and_publish.py` – Main training logic
- `src/scheduler.py` – Cron scheduler for 15-min cycles
- `requirements.txt` – scikit-learn, xgboost, mlflow, pandas, psycopg2
- `Dockerfile` – Builds trainer container

**Training Flow**:
```python
# 1. Read historical demand from Postgres
SELECT nodeId, sku, COUNT(*) AS demandCount, ...
  FROM inventory_ledger.inventory_reservations
  WHERE created_at >= now() - interval '90 days'
  GROUP BY nodeId, sku, date_trunc('1 hour', created_at)

# 2. Engineer features: rolling averages, seasonality, stock levels
features = [
  'demandLast1h', 'demandLast6h', 'demandLast24h',
  'stockLevel', 'reorderPoint', 'dayOfWeek', 'hourOfDay'
]

# 3. Train model (e.g., RandomForest or XGBoost)
model = RandomForestRegressor(n_estimators=50, max_depth=10)
model.fit(X_train, y_train)

# 4. Evaluate: MAE, RMSE, R²
mae = mean_absolute_error(y_val, y_pred)
rmse = sqrt(mean_squared_error(y_val, y_pred))
r2 = r2_score(y_val, y_pred)

# 5. Log to MLflow
mlflow.log_params({'n_estimators': 50, 'max_depth': 10})
mlflow.log_metrics({'mae': mae, 'rmse': rmse, 'r2': r2})
mlflow.sklearn.log_model(model, artifact_path='model')

# 6. Register and promote
mlflow.register_model(..., "inventory-demand-forecast")
mlflow.set_registered_model_alias(..., alias="champion")

# 7. Publish forecast snapshot to Postgres
INSERT INTO forecasting_planning.inventory_forecasts
  (nodeId, sku, predicted15mDemand, predicted24hDemand, ...)
  VALUES (...)
```

**Model Metrics** (from latest run):
- Training samples: 10,699
- Validation samples: 2,675
- MAE: 0.0076 units
- RMSE: 0.166 units
- R²: 0.0038 (note: sparse historical data means low variance; will improve with more demand history)

---

### 2. MLflow Tracking & Registry

**Purpose**: Track training experiments, manage model versions, promote champions for inference.

**Deployment**:
```bash
docker compose -f docker-compose.ml.yml up --build -d
```

**Services**:
- **MLflow Tracking Server**: http://localhost:5005
  - Experiment runs with metrics, params, artifacts
  - Model registry: inventory-demand-forecast (latest champion)
  - Searchable run history

**Supervisor Workflow**:
1. Navigate to http://localhost:5005
2. View experiment: "inventory-demand-forecast"
3. Compare model runs: metrics (MAE, RMSE, R²), hyperparameters
4. Promote best run: set alias to "champion" or "staging"
5. Forecasting service automatically reads latest champion snapshot

**Example MLflow Call**:
```python
with mlflow.start_run():
    mlflow.log_param("model_type", "RandomForest")
    mlflow.log_metric("mae", 0.0076)
    mlflow.sklearn.log_model(model, "model")
    mlflow.end_run()
```

---

### 3. Forecasting Service (Java Spring Boot, EXTERNAL_ML Mode)

**Purpose**: Exposes trained model predictions and forecast data to dashboard APIs. Does NOT train; only reads model metadata and forecast snapshots from Postgres.

**Configuration**:
```bash
export FORECASTING_PIPELINE_MODE=EXTERNAL_ML
mvn -q -pl services/intelligence/forecasting-planning-service spring-boot:run -Dspring-boot.run.profiles=local
```

**APIs** (all read-only in EXTERNAL_ML mode):

#### `GET /api/v1/model-runs/latest`
Returns metadata for the latest promoted champion model.

**Response**:
```json
{
  "modelRunId": "run-uuid-001",
  "modelVersion": 1,
  "modelAlias": "champion",
  "trainingSampleCount": 10699,
  "validationSampleCount": 2675,
  "mae": 0.007570,
  "rmse": 0.165948,
  "r2": 0.003787,
  "trainedAt": "2026-05-06T09:00:00Z",
  "publishedAt": "2026-05-06T09:01:30Z"
}
```

#### `GET /api/v1/forecast-runs/latest`
Returns metadata for the latest forecast run (published by trainer).

**Response**:
```json
{
  "forecastRunId": "forecast-run-uuid-001",
  "status": "COMPLETED",
  "forecastCount": 14,
  "generatedAt": "2026-05-06T09:01:30Z",
  "modelVersion": 1,
  "nextRefreshAt": "2026-05-06T09:16:00Z"
}
```

#### `GET /api/v1/forecasts?limit=5&nodeId=NODE-DELHI-01&status=HIGH_RISK`
Returns paginated forecast rows for dashboard display.

**Response**:
```json
{
  "items": [
    {
      "forecastId": "forecast-uuid-001",
      "nodeId": "NODE-DELHI-01",
      "sku": "SKU-WIDGET-001",
      "availableQuantity": 42,
      "predicted15mDemand": 3.2,
      "predicted24hDemand": 76.8,
      "daysOfCover": 0.55,
      "stockoutRisk": "HIGH_RISK",
      "recommendedReplenishmentQuantity": 100,
      "recommendedReorder": true,
      "forecastedAt": "2026-05-06T09:01:30Z"
    }
  ],
  "limit": 5,
  "offset": 0,
  "total": 14
}
```

#### `GET /api/v1/forecast-runs/latest/summary`
Returns aggregated KPIs for the ops dashboard (risk breakdown, replenishment candidates).

**Response**:
```json
{
  "totalForecasts": 14,
  "riskBreakdown": {
    "lowRisk": 12,
    "mediumRisk": 2,
    "highRisk": 0,
    "criticalRisk": 0
  },
  "totalRecommendedReplenishment": 200,
  "topReplenishmentCandidates": [
    {
      "nodeId": "NODE-DELHI-01",
      "sku": "SKU-WIDGET-001",
      "recommendedQuantity": 100,
      "daysOfCover": 0.55
    },
    {
      "nodeId": "NODE-BANGALORE-01",
      "sku": "SKU-GADGET-002",
      "recommendedQuantity": 100,
      "daysOfCover": 0.72
    }
  ],
  "generatedAt": "2026-05-06T09:01:30Z"
}
```

---

## Running the Stack

### Prerequisites
- Docker & Docker Compose
- Java 21
- Maven
- Postgres (infra running)

### Step 1: Start Infrastructure
```bash
docker compose -f docker-compose.infra.yml up -d
# Postgres on localhost:5432
```

### Step 2: Start ML Stack (MLflow + Trainer)
```bash
docker compose -f docker-compose.ml.yml up --build -d
# MLflow UI: http://localhost:5005
# Trainer runs every 15 minutes via cron
```

### Step 3: Start Forecasting Service
```bash
export FORECASTING_PIPELINE_MODE=EXTERNAL_ML
cd services/intelligence/forecasting-planning-service
mvn clean compile
JAVA_HOME=/path/to/java21 mvn spring-boot:run -Dspring-boot.run.profiles=local
# Service on http://localhost:8093
```

### Step 4: Verify
```bash
# Check forecasting service health
curl http://localhost:8093/actuator/health

# Check latest model metadata
curl http://localhost:8093/api/v1/model-runs/latest

# Check latest forecast data
curl http://localhost:8093/api/v1/forecasts?limit=5

# View MLflow UI (for supervisors)
open http://localhost:5005
```

---

## Supervision & Model Promotion

**For Data Scientists & Supervisors**:

1. **Monitor Training**: http://localhost:5005
   - View experiment runs in real time
   - Compare model versions: metrics, params, artifacts
   
2. **Promote Champion**:
   ```python
   mlflow.set_registered_model_alias(
       name="inventory-demand-forecast",
       alias="champion",
       version=2  # Promote version 2 to champion
   )
   ```
   Forecasting service automatically reads the new champion on next startup or refresh.

3. **Rollback**:
   ```python
   mlflow.set_registered_model_alias(
       name="inventory-demand-forecast",
       alias="champion",
       version=1  # Revert to version 1
   )
   ```

---

## Data Quality & Model Improvement

**Current Limitations**:
- Training sample size: ~10K reservations over 90 days
- Sparse historical demand: many nodes/SKUs have low transaction volume
- Predictions are mostly 0 with LOW risk (data problem, not integration issue)

**To Improve Model**:
1. **Increase demand history**: Run system longer to accumulate 180+ days of real transactions
2. **Add seasonal features**: Day-of-week, holiday calendar, promotional flags
3. **Node-specific models**: Train separate models per warehouse type (store vs. FC)
4. **Feature engineering**: Lead/lag features, inventory position, supplier lead times
5. **Hyperparameter tuning**: Use MLflow's experiment tracking to test different model configs

---

## Architecture Decisions

### Why MLflow (Not Feast)?
- **MLflow** handles experiment tracking + model registry + local serving in one unified tool
- **Feast** is powerful for large-scale feature engineering but assumes a separate feature-store infrastructure
- For this timeline and repo, MLflow provides the right balance: production-shaped without infrastructure overhead
- Sources: https://mlflow.org/docs/latest/ml/deployment/deploy-model-locally

### Why Separate Trainer + Service?
- **Trainer** (Python) owns model training, experiment logging, promotion workflow
- **Forecasting service** (Java) owns prediction serving and dashboard APIs
- Separation of concerns: data scientists iterate on Python models independently; dashboard developers iterate on API shape independently
- Production-standard pattern: similar split exists in platforms like Uber Michelangelo, Alibaba's PAI

### Why Local Docker (Not Cloud ML)?
- Capstone timeline doesn't justify cloud vendor lock-in
- Local deployment is reproducible, testable, and demonstrates full stack understanding
- Easy to move to cloud ML (SageMaker, Vertex AI, MLflow on Kubernetes) post-capstone without changing trainer logic

---

## Example: End-to-End Training Run

**Trainer Output** (from latest production run):

```
[2026-05-06 09:00:00] Starting training cycle...
[2026-05-06 09:00:05] Read 10,699 training samples from inventory_ledger
[2026-05-06 09:00:10] Training RandomForest(n_estimators=50, max_depth=10)...
[2026-05-06 09:00:20] Validation MAE: 0.0076, RMSE: 0.1659, R²: 0.0038
[2026-05-06 09:00:25] Logged run to MLflow (run_id: abc123)
[2026-05-06 09:00:30] Registered model: inventory-demand-forecast v1
[2026-05-06 09:00:35] Set alias champion -> v1
[2026-05-06 09:00:40] Published 14 forecast rows to forecasting_planning.inventory_forecasts
[2026-05-06 09:00:45] Cycle complete. Next run: 2026-05-06 09:15:00
```

**Forecasting Service Output** (at 09:01):

```
GET /api/v1/model-runs/latest
-> 200 OK
{
  "modelVersion": 1,
  "mae": 0.0076,
  "rmse": 0.1659,
  "r2": 0.0038,
  "trainedAt": "2026-05-06T09:00:20Z"
}

GET /api/v1/forecasts?limit=3
-> 200 OK
[
  { "nodeId": "NODE-DELHI-01", "sku": "SKU-WIDGET-001", "predicted24hDemand": 76.8, "stockoutRisk": "LOW_RISK" },
  { "nodeId": "NODE-BANGALORE-01", "sku": "SKU-GADGET-002", "predicted24hDemand": 45.2, "stockoutRisk": "LOW_RISK" },
  { "nodeId": "NODE-MUMBAI-01", "sku": "SKU-TOOL-003", "predicted24hDemand": 23.5, "stockoutRisk": "LOW_RISK" }
]
```

---

## References

- **MLflow Local Deployment**: https://mlflow.org/docs/latest/ml/deployment/deploy-model-locally
- **MLflow Model Registry**: https://mlflow.org/docs/latest/ml/model-registry/
- **MLflow Host Configuration**: https://mlflow.org/docs/3.5.0rc0/ml/tracking/server/security/configuration/
- **Sklearn RandomForest**: https://scikit-learn.org/stable/modules/generated/sklearn.ensemble.RandomForestRegressor.html
- **XGBoost**: https://xgboost.readthedocs.io/
