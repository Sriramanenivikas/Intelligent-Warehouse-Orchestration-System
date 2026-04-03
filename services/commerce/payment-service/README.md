# payment-service

Payment authorization and state management service.

What it does:

- authorizes payment attempts for an order workflow
- stores payment state in PostgreSQL
- enforces idempotency with Redis and persistence
- writes outbox events for downstream orchestration
- exposes health, metrics, and OTLP tracing

Local runtime:

- API: `http://localhost:8085`
- PostgreSQL: `localhost:55432`
- Redis: `localhost:56379`
- Kafka: `localhost:9092`

Endpoints:

- `POST /api/v1/payments`
- `GET /api/v1/payments/{paymentId}`
- `POST /api/v1/payments/{paymentId}/success`
- `POST /api/v1/payments/{paymentId}/fail`

Idempotency:

- require `Idempotency-Key` on mutating endpoints
- same key plus same request replays the same response
- same key plus different request returns conflict

Implementation status:

- production-style skeleton
- no external payment provider integration yet
- suitable for orchestrator integration in the next slice
