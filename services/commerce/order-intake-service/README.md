# order-intake-service

Fast order acceptance and idempotency boundary service

Capabilities:

- accepts order intents
- enforces idempotency
- persists order, items, and outbox event
- caches idempotent replays in Redis
- exposes health, metrics, and OTLP tracing

Local run:

```bash
docker compose up -d postgres redis
cd services/commerce/order-intake-service
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Local infra ports:

- PostgreSQL: `localhost:55432`
- Redis: `localhost:56379`

Smoke test:

```bash
curl -i -X POST http://localhost:8081/api/v1/order-intents \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: demo-key-001' \
  -d '{
    "customerId": "cust-1001",
    "channel": "APP",
    "paymentMode": "PREPAID",
    "currency": "INR",
    "deliveryAddress": {
      "name": "Vikas",
      "line1": "Sector 21",
      "line2": "Near metro",
      "city": "Gurugram",
      "state": "Haryana",
      "postalCode": "122001",
      "country": "IN",
      "phone": "9999999999"
    },
    "items": [
      {"sku": "SKU-APPLE-1KG", "quantity": 1},
      {"sku": "SKU-MILK-500ML", "quantity": 2}
    ]
  }'
```
