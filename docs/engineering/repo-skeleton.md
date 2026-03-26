# Repo Restructure Plan

This document defines how the current repository should evolve from the original service layout into the new target architecture.

This is a planning document only. It does not imply that all code has already been migrated.

## 1. Goal

Restructure the repository so it matches the target fulfillment platform instead of the original broad prototype layout.

The main change is:

- move from many loosely related services toward a smaller, clearer set of bounded-context services
- move edge concerns into platform infrastructure
- align service names with actual product responsibilities

## 2. Guiding Principles

1. `Keep service boundaries meaningful`
   Split where ownership and failure isolation justify it. Merge where boundaries are artificial.

2. `Do not create infrastructure services as business services`
   Gateway, discovery, and config belong to platform architecture, not to app-level domain modeling.

3. `Name services after business capabilities`
   Use names that describe operational ownership.

4. `Do not preserve old modules just because they exist`
   The target architecture wins over the original folder list.

## 3. Target Top-Level Repository Shape

```text
IWOS/
  docs/
  contracts/
    openapi/
    asyncapi/
    events/
  platform/
    kong/
    shared-java/
  services/
    commerce/
    fulfillment/
    network/
    intelligence/
  deploy/
    argocd/
    helm/
    envs/
  infra/
    terraform/
  tests/
    contract/
    integration/
    perf/
  runbooks/
  scripts/
```

## 4. Current To Target Service Mapping

| Current Module | Target Action | Target Module | Notes |
|---|---|---|---|
| `iwos-api-gateway` | retire from runtime | `platform/kong` | replace custom gateway with Kong-managed edge |
| `iwos-auth-service` | keep and rename | `services/commerce/identity-service` | auth and token concerns |
| `iwos-cart-service` | keep | `services/commerce/cart-service` | customer cart state |
| `iwos-catalog-service` | keep | `services/commerce/catalog-service` | item master and catalog views |
| `iwos-common` | refactor | `platform/shared-java` | shared kernel only, avoid business coupling |
| `iwos-config-server` | retire from target runtime | none | use platform config and secret management |
| `iwos-darkstore-service` | split | `node-service`, `inventory-ledger-service`, `warehouse-orchestrator-service` | dark store is a node type, not a full standalone domain by itself |
| `iwos-discovery-server` | retire from target runtime | none | use Kubernetes service discovery |
| `iwos-dispatch-service` | split and rename | `shipment-network-service` and `scan-event-service` | focus on shipment handoffs and milestones |
| `iwos-fraud-detection-service` | pause | later phase | keep out of critical path in initial build |
| `iwos-inventory-service` | reshape and strengthen | `services/fulfillment/inventory-ledger-service` | authoritative stock, reservation, ATP, ledger |
| `iwos-notification-service` | keep | `services/network/notification-service` | customer and ops notifications |
| `iwos-order-service` | split | `order-intake-service` and `order-orchestrator-service` | separate sync acceptance from async lifecycle |
| `iwos-payment-service` | keep | `services/commerce/payment-service` | payment auth, callbacks, COD policies |
| `iwos-pick-pack-service` | merge and reshape | `services/fulfillment/task-execution-service` | warehouse execution tasks |
| `iwos-pricing-service` | keep | `services/commerce/pricing-service` | price calculation and snapshot support |
| `iwos-recommendation-service` | pause | later phase | not core to order-to-delivery V1 |
| `iwos-returns-service` | keep | `services/fulfillment/returns-service` | reverse logistics |
| `iwos-route-optimizer-service` | pause | later phase | not on critical initial architecture path |
| `iwos-search-service` | keep as read-side | `services/commerce/search-service` | discovery/read model only |
| `iwos-serviceability-service` | merge | `services/commerce/promise-allocation-service` | promise, serviceability, node selection |
| `iwos-stock-predictor-service` | rename and expand | `services/intelligence/forecasting-planning-service` | AI-driven demand and replenishment planning |
| `iwos-tracking-service` | split | `scan-event-service` and `analytics-read-service` | tracking becomes scan milestone normalization and read models |
| `iwos-wms-service` | split and reshape | `warehouse-orchestrator-service` and `task-execution-service` | move from generic WMS to explicit orchestration and execution |

## 5. Target Core Service Catalog

### Commerce

- `identity-service`
- `cart-service`
- `catalog-service`
- `search-service`
- `pricing-service`
- `order-intake-service`
- `promise-allocation-service`
- `payment-service`
- `order-orchestrator-service`

### Fulfillment

- `inventory-ledger-service`
- `node-service`
- `warehouse-orchestrator-service`
- `task-execution-service`
- `returns-service`

### Network

- `shipment-network-service`
- `scan-event-service`
- `notification-service`

### Intelligence

- `forecasting-planning-service`
- `analytics-read-service`

### Platform

- `Kong Gateway`
- shared Java libraries in `platform/shared-java`
- CI/CD, GitOps, Terraform, and environment manifests outside the business-service layer

## 6. Why This Shape

This target structure is chosen because it matches actual product capabilities:

- commerce intake
- promise and payment
- fulfillment execution
- shipment network operations
- planning and intelligence

It avoids:

- infrastructure services pretending to be domain services
- duplicated responsibilities
- an overly fragmented service list that is expensive to reason about and operate

## 7. Suggested Package Layout Inside Each Service

Every service should converge toward this package layout:

```text
src/main/java/com/iwos/<service>/
  api/
  application/
  domain/
  infrastructure/
  config/
```

### Responsibility by package

- `api`: controllers, request/response DTOs, API adapters
- `application`: use cases, orchestration inside the service boundary
- `domain`: aggregates, entities, domain services, business rules
- `infrastructure`: persistence, Kafka, external adapters, AWS integrations
- `config`: framework and bean wiring

## 8. Migration Order

### Phase 1

- establish canonical docs
- freeze target names and service boundaries
- stop designing around `api-gateway`, `config-server`, and `discovery-server`

### Phase 2

- split `order-service`
- reshape `inventory-service`
- create `promise-allocation-service`
- split `wms-service` and `pick-pack-service`

### Phase 3

- reshape dispatch and tracking into shipment and scan-event services
- fold dark-store concerns into node, inventory, and warehouse domains
- promote forecasting service to planning service

### Phase 4

- move shared code into platform libraries
- add contracts, runbooks, and perf-test layout
- align deployment structure with target services

## 9. What Should Not Happen During Restructure

- do not keep legacy services only because they already compile
- do not let shared libraries become dumping grounds for domain logic
- do not rebuild every possible feature before the core order-to-delivery path is stable
- do not add more edge infrastructure into Java app modules

## 10. Related Canonical Docs

- [Project Context](project-context.md)
- [Engineering Handbook](engineering-handbook.md)
- [Unified Fulfillment Platform](hld/unified-fulfillment-platform.md)
- [Production Architecture Decision Record](hld/production-architecture-decisions.md)
