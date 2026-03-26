# Project Context

This document explains what this repository is trying to become.

It exists for three audiences:

- a new engineer joining the project
- a reviewer evaluating the capstone or architecture
- another AI model that needs enough context to continue work without additional prompting

## 1. Product Definition

IWOS is evolving into a unified fulfillment platform inspired by four operational models:

- `Blinkit`: dark stores, fast replenishment, express picking, short delivery promises
- `Amazon`: fulfillment centers, broad catalog, pick-pack-ship, distributed inventory network
- `Delhivery/FedEx`: hub-and-spoke parcel movement with scan milestones and operational handoffs
- `AI planning systems`: demand forecasting, stock placement, replenishment recommendations, SLA-risk prediction

This is not meant to be only:

- an ecommerce storefront
- a pure warehouse management system
- a pure carrier system
- a toy microservices demo

The target is a production-style `order-to-delivery fulfillment platform` with AI-assisted planning.

## 2. System Boundary

The intended system starts at:

- customer order placement

and ends at:

- delivery confirmation

The system includes:

- order acceptance
- payment processing
- promise and node allocation
- inventory reservation
- warehouse task orchestration
- shipment and hub movement
- delivery station handoff
- scan-based status updates
- reverse logistics
- demand and replenishment planning

The system explicitly excludes GPS-heavy live tracking in V1.

## 3. Scale Target

The target is:

- `1,000+ accepted orders/sec per regional cell`

This phrase is important.

It does **not** mean:

- `1,000+ orders/sec fully completed end-to-end in one synchronous HTTP request`

It **does** mean:

- the system can durably accept orders at that rate
- internal processing continues asynchronously through queues, state machines, and workers
- global scale is achieved by adding more regional cells, not by building one global hot-path database

## 4. Operational Model

The target system follows five core ideas:

1. `Fast accept path`
   The request path should validate, snapshot, persist order intent, publish an event, and return quickly.

2. `Regional cell architecture`
   Each region should own its own order intake, event backbone, transactional data, and fulfillment operations.

3. `Local stock ownership`
   Inventory is owned by nodes such as dark stores, FCs, hubs, and delivery stations. No global inventory lock belongs on the hot path.

4. `Warehouse and logistics are event-driven`
   Internal truth comes from reservations, tasks, and scan events, not from one long blocking workflow.

5. `AI supports operations`
   AI should improve planning and decision-making. It should not make the hot path slow or fragile.

## 5. Architecture Summary

The chosen target architecture is:

- `Kong` at the edge
- `EKS` for service runtime
- `MSK / Kafka` for event backbone
- `Aurora PostgreSQL` for transactional domains
- `Redis` for idempotency, caching, and hot operational state
- `DynamoDB` for very high-write timeline and online feature use cases
- `S3` as the data lake for history and model training

The system is designed as a set of bounded-context services rather than a single monolith or a large set of tiny premature microservices.

## 6. Business Flows Covered

The target platform needs to support three operational branches:

### Express branch

Used for Blinkit-style orders:

- order placed
- dark store selected
- local inventory reserved
- rapid pick and pack
- dispatch to rider or local delivery flow
- delivery confirmed by milestone events

### Fulfillment-center branch

Used for Amazon-style orders:

- order placed
- FC selected
- inventory reserved
- wave or task planning
- pick, pack, label, manifest
- shipment into network
- station handoff
- delivery confirmation

### Parcel-network branch

Used for Delhivery/FedEx-style movement:

- shipment created
- origin hub receive
- sortation
- linehaul departure and arrival
- destination hub receive
- delivery station receive
- out-for-delivery milestone
- delivered or failed attempt milestone

## 7. AI Scope

AI is part of the platform, but it is not the center of every request.

AI should be used for:

- demand forecasting by SKU, node, and time window
- stock placement recommendations
- replenishment recommendations
- SLA-risk scoring
- allocation ranking support
- capacity and labor planning

AI should not be used to:

- block every request with heavy offline models
- replace the transactional system of record
- become an excuse to avoid explicit business rules

## 8. Current Repo Reality

The current repository is not yet this final production system.

It is best understood as:

- a broad prototype with many service folders
- partial domain modeling
- uneven implementation depth
- original docs that describe an earlier architecture

The codebase contains useful domain material, but it should not be mistaken for a finished production architecture.

The canonical future design is defined by:

- [Unified Fulfillment Platform](unified-fulfillment-platform.md)
- [Production Architecture Decision Record](../decisions/production-architecture-decisions.md)
- [Repo Restructure Plan](repo-restructure-plan.md)

## 9. In Scope For The New Architecture

- order placement through delivery confirmation
- dark store, FC, hub, and delivery station operations
- payments and COD policy handling
- inventory reservation and release
- warehouse tasks: receive, putaway, replenish, pick, pack, ship
- scan milestone ingestion and normalized status progression
- returns and reverse logistics
- observability, security, compliance boundaries, and DevOps
- AI-driven planning and forecasting

## 10. Out Of Scope For The First Serious Version

- GPS live tracking
- recommendation engine as a core dependency
- full event sourcing everywhere
- route optimization in the hot order path
- fully generalized seller marketplace workflows
- advanced pricing experimentation on every checkout request

## 11. Success Criteria

The architecture should be judged successful if it can support:

- a clear and defensible system boundary
- a realistic path to `1k+ accepted orders/sec per regional cell`
- fault isolation by region and by bounded context
- reproducible deployment on AWS
- operational visibility and rollback safety
- a clean service map that engineers can implement incrementally
- a clear AI integration story with real business value

## 12. Canonical Reading Order

Read these next:

1. [Engineering Handbook](../engineering/engineering-handbook.md)
2. [Repo Restructure Plan](repo-restructure-plan.md)
3. [Unified Fulfillment Platform](unified-fulfillment-platform.md)
4. [Production Architecture Decision Record](../decisions/production-architecture-decisions.md)
