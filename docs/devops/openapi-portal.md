# Global OpenAPI Portal

This repository now supports a global aggregated Swagger UI for all real runtime services.

How it works:

- each real Spring Boot service exposes its own generated OpenAPI at `/v3/api-docs`
- the global portal uses a single Swagger UI container
- the portal loads all per-service specs from `infra/openapi/swagger-config.json`

Run the portal:

```bash
docker compose -f docker-compose.openapi.yml up -d
```

Open:

- `http://localhost:8099`

Requirements:

- the target services must be running locally on their configured ports
- if a service is down, that service entry in the portal will not load

Current aggregated services:

- order-intake-service
- inventory-ledger-service
- order-orchestrator-service
- promise-allocation-service
- payment-service
- warehouse-orchestrator-service
- task-execution-service
- shipment-handoff-service
- shipment-network-service
- scan-event-service
- notification-service
- identity-service
- forecasting-planning-service

What this is not:

- this is not one merged OpenAPI spec
- this is one global UI that aggregates multiple microservice specs

Why this approach:

- it matches microservice boundaries
- it avoids stale giant merged specs
- it lets each service own its runtime contract
