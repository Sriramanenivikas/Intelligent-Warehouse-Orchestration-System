# 📚 IWOS Architecture Documentation

## High-Level Design (HLD)

| Document | Description |
|----------|-------------|
| [System Architecture](hld/system-architecture.md) | Complete system overview, service domains, communication patterns |
| [Data Flow](hld/data-flow.md) | End-to-end request flows: Order, Blinkit 10-min, Payment |
| [Event-Driven Architecture](hld/event-driven-architecture.md) | Kafka topics, event schemas, choreography vs orchestration |
| [Deployment Architecture](hld/deployment-architecture.md) | AWS EKS, multi-AZ, CDN, auto-scaling |
| [Security Architecture](hld/security-architecture.md) | Auth flows, mTLS, WAF, encryption at rest/transit |

## Low-Level Design (LLD)

| Document | Description |
|----------|-------------|
| [Order Service LLD](lld/order-service-lld.md) | CQRS + Event Sourcing, saga orchestration, class/sequence diagrams |
| [Inventory Service LLD](lld/inventory-service-lld.md) | Stock management, reservation, warehouse allocation |
| [Payment Service LLD](lld/payment-service-lld.md) | Strategy pattern, gateway abstraction, ledger |
| [Dark Store Service LLD](lld/darkstore-service-lld.md) | Blinkit model: micro-fulfillment, batch picking, replenishment |
| [Catalog Service LLD](lld/catalog-service-lld.md) | Product hierarchy, SKU generation, search sync |
| [Database Schema](lld/database-schema.md) | ER diagrams for all services |

## API Contracts

| Document | Description |
|----------|-------------|
| [API Contracts](api/api-contracts.md) | REST API summary for all 26 services |
