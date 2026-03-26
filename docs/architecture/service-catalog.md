# Service Catalog

## Commerce

- `identity-service`: authentication, authorization, token issuance
- `catalog-service`: item master and catalog publication
- `search-service`: customer-facing search read models
- `pricing-service`: pricing calculation and snapshot generation
- `cart-service`: cart state and checkout draft handling
- `order-intake-service`: fast order acceptance and idempotency boundary
- `order-orchestrator-service`: asynchronous order lifecycle state machine
- `promise-allocation-service`: promise, serviceability, and node selection
- `payment-service`: payment auth, webhook, capture, and COD policy

## Fulfillment

- `inventory-ledger-service`: authoritative stock, reservations, ATP, stock ledger
- `node-registry-service`: dark store, FC, hub, and station metadata
- `warehouse-orchestrator-service`: inbound, replenishment, and outbound orchestration
- `task-execution-service`: warehouse work execution and exception handling
- `returns-service`: reverse logistics and disposition flows

## Network

- `shipment-network-service`: manifesting, linehaul, hub, and station handoffs
- `scan-event-service`: milestone ingestion and normalized scan timeline
- `notification-service`: customer and ops notifications

## Intelligence

- `forecasting-planning-service`: demand forecast, replenishment, stock placement planning
- `feature-platform-service`: online and offline feature serving
- `control-tower-service`: operational read models, dashboards, SLA exception views

## Platform

- `common-kernel`: shared ids, contracts, and exceptions
- `observability-starter`: tracing, logging, metrics conventions
- `kafka-starter`: event publishing and consumer conventions
- `service-testkit`: shared testing utilities and fixtures
- `kong`: edge gateway configuration and policy plane
