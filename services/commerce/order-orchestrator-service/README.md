# order-orchestrator-service

Coordinates accepted order intents into downstream inventory reservations.

## Responsibilities

- discover pending `order-intake.accepted.v1` events from `public.outbox_events`
- load accepted order intents and line items from the order-intake schema
- reserve stock in `inventory-ledger-service`
- compensate successful reservations if a later reservation fails
- persist orchestration workflow state and downstream reservation references
- emit orchestration outbox events for downstream consumers

## Local Run

```bash
JAVA_HOME=/Users/vikas/Library/Java/JavaVirtualMachines/temurin-21.0.10/Contents/Home \
ORDER_ORCHESTRATOR_LOG_FILE=/Users/vikas/Documents/capstone/IWOS/logs/order-orchestrator-service.log \
mvn -q spring-boot:run -Dspring-boot.run.profiles=local
```

## Local API

- `POST /api/v1/order-workflows/process-pending`
- `POST /api/v1/order-workflows/{orderIntentId}/process`
- `GET /api/v1/order-workflows/{orderIntentId}`

## Local Smoke Flow

```bash
curl -s -X POST 'http://localhost:8083/api/v1/order-workflows/process-pending?limit=10'
```
