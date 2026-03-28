# inventory-ledger-service

Inventory ledger, stock adjustment, and reservation service

Capabilities:

- accepts stock adjustments
- exposes stock availability by node and SKU
- creates inventory reservations
- confirms and releases reservations
- persists ledger, reservation, idempotency, and outbox records
- exposes health, metrics, and OTLP tracing

Local run:

```bash
docker compose up -d postgres redis
cd services/fulfillment/inventory-ledger-service
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Local infra ports:

- PostgreSQL: `localhost:55432`
- Redis: `localhost:56379`

Smoke test:

```bash
curl -i -X POST http://localhost:8082/api/v1/stock-adjustments \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: stock-adjust-001' \
  -d '{
    "nodeId": "NODE-DELHI-01",
    "sku": "SKU-APPLE-1KG",
    "quantityDelta": 20,
    "reason": "INITIAL_LOAD",
    "referenceType": "MANUAL",
    "referenceId": "seed-001"
  }'
```
