# Unified Fulfillment Platform

Production-style architecture proposal for a combined:

- Blinkit model: dark stores, express picking, rapid replenishment
- Amazon model: FC allocation, pick-pack-ship, broad inventory network
- Delhivery/FedEx model: hub-and-spoke parcel movement with scan milestones
- AI model: demand forecasting, stock placement, replenishment planning, SLA risk scoring

This document is a new target architecture, not a patch over the current repo shape.

## 1. Goal

Build a full fulfillment platform from `order placement` to `delivery confirmation` without GPS tracking, designed for:

- `1,000+ accepted orders/sec` per regional cell
- global scale by replicating regional cells, not by running one global hot-path database
- scan-based warehouse and logistics visibility
- AI-assisted planning and decision support
- production-style reliability patterns: idempotency, outbox, inbox dedupe, retries, DLQ, reservations, auditability

## 2. Design Principles

1. `Order acceptance must be fast and small`
   The request path only validates, snapshots, writes a durable order intent, and returns an acknowledgement.

2. `Regional cell architecture`
   Each region owns its own order intake, inventory, warehouse, hub, and delivery processing. Global scale is achieved by adding more cells.

3. `Local inventory ownership`
   Stock is owned by fulfillment nodes: dark stores, FCs, hubs, stations. No global inventory lock on the hot path.

4. `Warehouse is task-driven`
   Real operational truth comes from task events and scan events, not from long synchronous workflows.

5. `Scan milestones instead of GPS`
   Order progress is derived from pick, pack, manifest, hub receive, sort, linehaul, station receive, out-for-delivery, delivered scans.

6. `AI supports operations, not the other way around`
   Models must help promise, allocation, replenishment, and SLA management without blocking the hot path with expensive inference.

7. `Use event-driven transactional design`
   Prefer `OLTP + outbox + Kafka + state machine` over full event sourcing for the first production-grade implementation.

## 3. Scope

Included:

- Customer order placement
- Payment authorization and confirmation
- Inventory reservation
- Dark store fulfillment
- FC fulfillment
- Hub-and-spoke parcel movement
- Delivery station handoff
- Delivery confirmation
- Warehouse inbound, replenishment, pick, pack, ship
- AI forecasting and planning

Excluded from V1:

- GPS live tracking
- advanced recommendation engine
- customer review platform
- marketplace seller settlement workflows
- dynamic pricing as a checkout dependency
- route optimization in the user-facing order path

## 4. Top-Level Architecture

```text
Customer Channels
  -> Global Edge
  -> Regional API Gateway
  -> Order Intake Cell

Regional Control Plane
  -> Promise & Allocation
  -> Payment
  -> Inventory Ledger
  -> Order State Manager
  -> Warehouse Orchestrator
  -> Shipment Network
  -> Scan Event Processing
  -> Notification

Execution Plane
  -> Dark Stores
  -> Fulfillment Centers
  -> Sortation Hubs
  -> Delivery Stations
  -> Scanner / RF / Workstation clients

Data & AI Plane
  -> Kafka / Event Backbone
  -> Aurora PostgreSQL
  -> Redis
  -> DynamoDB
  -> S3 Data Lake
  -> SageMaker / ML training and inference
  -> BI / Monitoring / Ops dashboards
```

## 5. Regional Cell Pattern

The system should be deployed as independent regional cells.

Example:

- `ap-south-1`: India cell
- `eu-west-1`: Europe cell
- `us-east-1`: North America cell

Each regional cell contains:

- its own API gateway
- its own Kafka cluster or regional event backbone
- its own transactional databases
- its own fulfillment nodes
- its own AI online feature cache

Global services should be limited to:

- traffic routing
- identity federation
- catalog replication
- global reporting
- model artifact distribution

The hot path must never depend on a cross-region synchronous call.

## 6. Service Map

| Service | Responsibility | Sync/Async Role | Primary Store |
|---|---|---|---|
| `api-gateway` | Auth edge, rate limiting, request routing | Sync | Redis for rate limits |
| `order-intake-service` | Accept order, validate idempotency, snapshot request, create order intent | Sync | Aurora PostgreSQL + Redis |
| `order-state-service` | Owns order lifecycle and orchestration state machine | Async | Aurora PostgreSQL |
| `promise-allocation-service` | Computes ETA promise, fulfillment type, node selection | Sync + Async | Redis + Aurora PostgreSQL |
| `payment-service` | Payment auth, webhook processing, COD policy | Sync + Async | Aurora PostgreSQL |
| `inventory-ledger-service` | Atomic stock reservation, release, stock ledger, ATP | Async | Aurora PostgreSQL |
| `node-registry-service` | Metadata for FCs, dark stores, hubs, delivery stations | Sync | Aurora PostgreSQL |
| `warehouse-orchestrator-service` | Creates inbound, replenishment, pick, pack, ship workflows | Async | Aurora PostgreSQL |
| `task-execution-service` | Worker task queue, scanner task acknowledgement, short-pick handling | Async | Aurora PostgreSQL + DynamoDB |
| `shipment-network-service` | Manifesting, carrier lanes, hub handoff, linehaul events | Async | Aurora PostgreSQL |
| `scan-event-service` | Ingests and normalizes scan milestones across network | Async | DynamoDB + S3 |
| `notification-service` | Customer and ops notifications | Async | Aurora PostgreSQL or DynamoDB |
| `forecasting-planning-service` | Demand forecast, replenishment recommendation, stock placement | Async + batch inference | S3 + feature store + model store |
| `feature-platform-service` | Online features for allocation, SLA risk, replenishment, fraud-lite | Sync | Redis / DynamoDB |
| `ops-control-tower-service` | Real-time operational dashboards and exception monitoring | Async read side | ClickHouse / OpenSearch / warehouse |

Optional upstream services:

- `auth-service`
- `catalog / item-master-service`
- `cart-service`

## 7. Data Stores

| Data Type | Recommended Store | Why |
|---|---|---|
| order intents, order state, payments, reservations, tasks, shipments | Aurora PostgreSQL | strong transactional consistency |
| idempotency keys, promise cache, hot capacity cache, online features | Redis | low latency reads/writes |
| high-volume scan events, task acknowledgements, scan timelines | DynamoDB | scalable event ingestion |
| event streaming backbone | Kafka / MSK | decoupled async processing |
| historical events, training data, audit archive | S3 | cheap durable storage |
| analytical dashboards | ClickHouse / Redshift / Athena | ops and BI queries |
| customer/product search if needed | OpenSearch | non-transactional search only |

## 8. End-to-End Order Flow

### 8.1 Fast Accept Path

The request path must stay minimal.

```text
Client
 -> API Gateway
 -> Order Intake Service
    -> validate JWT / session
    -> validate idempotency key
    -> read cart snapshot
    -> read serviceability / promise snapshot
    -> read price snapshot
    -> call fast allocation scoring
    -> write order_intent + item_snapshot + address_snapshot + outbox
 <- ORDER_ACCEPTED
```

The API returns:

- `orderId`
- `acceptedAt`
- `paymentStatus`
- `promiseWindow`
- `fulfillmentType`

The API should not wait for:

- warehouse picking
- full inventory completion across all items
- dispatch assignment
- carrier network progress

### 8.2 Async Commit Path

After acceptance:

```text
order.accepted
 -> payment authorization
 -> inventory reservation
 -> promise/allocation confirmation
 -> order state transition
 -> warehouse release
 -> shipment / manifest creation
 -> network scan milestones
 -> delivery confirmation
```

## 9. Express Flow: Blinkit Model

Use this for:

- local radius delivery
- fast-moving SKUs
- dark-store inventory
- low item-count orders

### Sequence

```text
Order accepted
 -> Promise engine selects dark store
 -> Inventory ledger reserves stock in that dark store
 -> Warehouse orchestrator creates express pick task
 -> Task execution assigns picker
 -> Picker scans picked items
 -> Pack task created
 -> Shipment network creates store-to-customer dispatch bag / manifest
 -> Delivery station scan or rider handoff scan marks OFD
 -> Delivery confirmation scan marks DELIVERED
```

### Key Rules

- keep pick faces pre-stocked
- auto-replenish from reserve bin to pick bin
- batch nearby express orders when operationally useful
- order allocation must consider store load and picker queue depth

## 10. Standard FC Flow: Amazon Model

Use this for:

- wide assortment orders
- multi-item orders
- non-express delivery windows
- orders requiring FC allocation

### Sequence

```text
Order accepted
 -> Promise engine selects FC
 -> Inventory ledger reserves FC stock
 -> Warehouse orchestrator assigns wave / batch / single-order pick
 -> Pick task generated by zone or aisle
 -> Worker scans picks
 -> Exceptions raised for short pick / mismatch / damage
 -> Pack task generated
 -> Label + manifest generated
 -> Shipment handed over to hub network
 -> Hub scans and delivery station scans drive status
 -> Delivery confirmation scan completes order
```

### Key Rules

- do not use customer request path for wave planning
- compute waves asynchronously every few minutes or by queue threshold
- allow single-order release for SLA-critical orders
- keep order state separate from warehouse task state

## 11. Hub-and-Spoke Parcel Flow: Delhivery/FedEx Model

This is the carrier and network layer.

### Node Types

- `origin FC / dark store`
- `sortation hub`
- `linehaul lane`
- `delivery station`

### Scan-Based Milestones

```text
PACKED
 -> MANIFESTED
 -> HUB_RECEIVED
 -> HUB_SORTED
 -> LINEHAUL_DEPARTED
 -> LINEHAUL_ARRIVED
 -> STATION_RECEIVED
 -> OUT_FOR_DELIVERY
 -> DELIVERED
```

No GPS dependency is required. Status comes from scan events.

### Network Logic

- hubs own bag/container and parcel scans
- lanes own departure and arrival scans
- stations own OFD and delivered scans
- every scan updates shipment state and order projection

## 12. Warehouse Orchestration Flows

Warehouse orchestration is the heart of the platform.

### 12.1 Inbound

```text
ASN created
 -> dock appointment scheduled
 -> truck arrival scan
 -> receiving scan
 -> QC outcome
 -> putaway task
 -> putaway completion scan
 -> inventory ledger increment
```

### 12.2 Replenishment

```text
AI or rule detects low pick-face stock
 -> replenishment task created
 -> worker moves stock reserve -> pick bin
 -> scan confirm source bin and destination bin
 -> inventory ledger movement posted
```

### 12.3 Outbound

```text
reserved order lines
 -> wave or task release
 -> pick
 -> short-pick exception if needed
 -> pack
 -> manifest
 -> stage at dock
 -> handoff scan
```

### 12.4 Reverse Logistics

```text
return initiated
 -> return package received at node
 -> inspect
 -> disposition: restock / quarantine / scrap / vendor return
 -> inventory ledger adjustment
```

## 13. AI Integration

AI should be split into online, near-real-time, and offline workloads.

### 13.1 AI Use Cases

| Use Case | When Used | Output |
|---|---|---|
| demand forecasting | hourly / daily batch | SKU-node demand forecast |
| stock placement | periodic planning | how much stock to place in dark store vs FC |
| replenishment recommendation | near-real-time | move stock from reserve to pick-face |
| SLA risk scoring | order accept + milestone updates | risk that order misses promise |
| allocation ranking | order accept | best node for fulfillment |
| labor planning | shift planning | staffing need by node and time window |

### 13.2 Model Placement

| Model | Invocation Path | Latency Budget |
|---|---|---|
| allocation ranker | sync during order accept | `< 20 ms` from cache-backed service |
| SLA risk model | sync or near-real-time | `< 30 ms` |
| demand forecast | offline batch | minutes to hours |
| replenishment policy model | streaming / scheduled | seconds to minutes |
| stock placement optimizer | batch | minutes |

### 13.3 Input Features

- historical demand by `sku x node x hour`
- weekday / weekend
- time of day
- festivals / holidays
- promotions and price bands
- weather and local events
- picker backlog
- node capacity and utilization
- supplier lead time
- historical stockouts
- short-pick rates
- lane congestion

### 13.4 AI Data Pipeline

```text
OLTP events
 -> Kafka
 -> S3 raw event lake
 -> ETL / feature generation
 -> training datasets
 -> SageMaker training
 -> model registry
 -> batch forecasts and online inference endpoints
 -> Redis / Dynamo online feature store
```

### 13.5 Recommended Capstone Models

1. `Demand Forecast Model`
   Predicts `SKU x node x hour/day`.

2. `Replenishment Recommendation Model`
   Predicts whether a node will face near-term pick-face shortage.

3. `Allocation Ranker`
   Scores dark store vs FC vs alternate node based on stock, load, SLA, and distance band.

4. `SLA Risk Model`
   Predicts whether the order is likely to miss promised delivery based on node load and network events.

## 14. Core Topics and Event Contracts

Recommended Kafka topics:

- `order.accepted`
- `order.payment.requested`
- `order.payment.authorized`
- `order.payment.failed`
- `inventory.reserve.requested`
- `inventory.reserved`
- `inventory.reserve.rejected`
- `allocation.decided`
- `warehouse.wave.created`
- `warehouse.task.created`
- `warehouse.task.completed`
- `warehouse.exception.created`
- `shipment.created`
- `shipment.manifested`
- `network.scan.received`
- `delivery.completed`
- `return.created`
- `forecast.input`
- `forecast.output`
- `planning.replenishment.recommended`
- `dlq.*`

Partitioning guidance:

- order topics keyed by `orderId`
- inventory topics keyed by `nodeId#skuId`
- task topics keyed by `nodeId#taskType`
- network scan topics keyed by `shipmentId`

## 15. Transaction and Reliability Patterns

### Mandatory Patterns

- `idempotency key` on order placement and payment initiation
- `outbox table` for all state-changing producers
- `inbox / dedupe table` for consumers
- `reservation TTL` for inventory holds
- `atomic stock decrement` in inventory ledger
- `DLQ + retry with backoff`
- `audit trail` for order, inventory, task, shipment state changes

### Recommended Transaction Strategy

Do not use distributed XA transactions.

Use:

- local database transaction per service
- outbox event publication
- state machine in `order-state-service`
- compensating actions:
  - release reservation
  - cancel warehouse tasks
  - reverse shipment booking if possible

### Inventory Reservation Rule

Inventory reservation must be atomic. Use a statement equivalent to:

```sql
UPDATE inventory_balance
SET available_qty = available_qty - :qty,
    reserved_qty = reserved_qty + :qty
WHERE node_id = :node_id
  AND sku_id = :sku_id
  AND available_qty >= :qty;
```

Never use naive read-modify-write for inventory on the hot path.

## 16. Order State Model

Recommended order states:

- `PLACED`
- `ACCEPTED`
- `PAYMENT_PENDING`
- `PAYMENT_AUTHORIZED`
- `RESERVATION_PENDING`
- `RESERVED`
- `ALLOCATED`
- `PICK_RELEASED`
- `PICKED`
- `PACKED`
- `MANIFESTED`
- `IN_NETWORK`
- `STATION_RECEIVED`
- `OUT_FOR_DELIVERY`
- `DELIVERED`
- `CANCELLED`
- `FAILED`
- `RETURN_INITIATED`
- `RETURN_RECEIVED`
- `RETURN_DISPOSITIONED`

Keep separate state machines for:

- order lifecycle
- shipment lifecycle
- warehouse task lifecycle

## 17. Capacity Model for 1k+/sec

### Design Assumptions

- `1,000 order accepts/sec` sustained per regional cell
- average `4-6 items/order`
- average `8-12 downstream events/order`
- average `3-8 scan milestones/order`

This implies:

- `8k-12k+` operational events/sec
- `3k-8k+` scan or milestone events/sec
- significantly higher internal service traffic than front-door RPS

### Scaling Strategy

Order accept scale:

- stateless `order-intake-service`
- autoscale on RPS, CPU, and p95 latency
- Redis-backed idempotency
- efficient append-oriented order intent writes

Inventory scale:

- partition inventory by node and SKU
- reserve locally
- avoid cross-node locking

Warehouse scale:

- queue-based task creation
- node-local worker pools
- scan ingestion horizontally scalable

Network scale:

- scan events ingest independently of order request path
- shipment read models built asynchronously

### Global Scale

If you want `10k+/sec worldwide`, do not design one global cluster. Use:

- `10 cells x 1k/sec`
- or `5 cells x 2k/sec`

Global load is an orchestration problem, not a single-database problem.

## 18. AWS Deployment Blueprint

### Global

- Route 53 latency-based routing
- AWS WAF
- Global Accelerator or CloudFront for edge optimization

### Per Regional Cell

- EKS across `3 AZs`
- ALB for ingress
- MSK for event streaming
- Aurora PostgreSQL clusters by domain:
  - orders
  - payments
  - inventory
  - warehouse
  - shipment
- ElastiCache Redis cluster
- DynamoDB for scan-event timelines and online features
- S3 for event lake and model data
- SageMaker for training and managed inference
- Managed Prometheus / Grafana / OpenTelemetry

### Suggested Domain Databases

| Domain | DB |
|---|---|
| order-intake + order-state | Aurora PostgreSQL |
| payment | Aurora PostgreSQL |
| inventory-ledger | Aurora PostgreSQL |
| warehouse-orchestrator + task-execution | Aurora PostgreSQL |
| shipment-network | Aurora PostgreSQL |
| scan-event-service | DynamoDB |
| online feature store | Redis / DynamoDB |
| analytics and lake | S3 + Athena / Redshift / ClickHouse |

## 19. Security and Operations

Security:

- JWT or OAuth at edge
- service-to-service mTLS inside mesh if needed
- KMS encryption for DB, Kafka, S3
- secrets in AWS Secrets Manager
- PII minimization in events

Operational requirements:

- trace every order by `orderId`
- trace every shipment by `shipmentId`
- trace every inventory move by `movementId`
- trace every scan by `scanId`
- alert on:
  - reservation failure spikes
  - short-pick spikes
  - node backlog growth
  - SLA risk distribution shift
  - hub scan latency

## 20. How This Maps to the Current Repo

Recommended repo realignment:

| Current Module | Recommended Action |
|---|---|
| `iwos-order-service` | split into `order-intake-service` and `order-state-service` |
| `iwos-inventory-service` | rebuild as `inventory-ledger-service` |
| `iwos-darkstore-service` | keep and evolve for express node fulfillment |
| `iwos-wms-service` | merge into `warehouse-orchestrator-service` |
| `iwos-pick-pack-service` | evolve into `task-execution-service` |
| `iwos-dispatch-service` | replace with `shipment-network-service` |
| `iwos-tracking-service` | replace with `scan-event-service` |
| `iwos-serviceability-service` | merge into `promise-allocation-service` |
| `iwos-payment-service` | keep, implement fully |
| `iwos-catalog-service` | retain as `item-master-service` |
| `iwos-stock-predictor-service` | repurpose as `forecasting-planning-service` |
| `iwos-notification-service` | keep |
| `iwos-auth-service` | keep if auth is in scope |
| `iwos-route-optimizer-service` | defer |
| `iwos-pricing-service` | defer from critical path |
| `iwos-recommendation-service` | remove from fulfillment scope |
| `iwos-review-service` | remove from fulfillment scope |
| `iwos-seller-service` | defer unless marketplace settlement is required |
| `iwos-config-server` | remove if on Kubernetes |
| `iwos-discovery-server` | remove if on Kubernetes |

## 21. Build Plan

### Phase 1: Core Transaction Path

Build first:

- `api-gateway`
- `order-intake-service`
- `order-state-service`
- `payment-service`
- `inventory-ledger-service`
- `promise-allocation-service`
- `node-registry-service`
- Kafka + outbox + idempotency

Outcome:

- order can be accepted, paid, reserved, allocated

### Phase 2: Warehouse Orchestration

Build next:

- `warehouse-orchestrator-service`
- `task-execution-service`
- inbound
- replenishment
- pick / pack / manifest
- scanner workflows

Outcome:

- orders become real warehouse work

### Phase 3: Network and Delivery Milestones

Build next:

- `shipment-network-service`
- `scan-event-service`
- delivery station milestones
- notification integration

Outcome:

- end-to-end non-GPS delivery visibility

### Phase 4: AI Planning Layer

Build next:

- demand forecast
- replenishment recommendation
- allocation ranker
- SLA risk scoring

Outcome:

- planning and promise quality improve

### Phase 5: Production Hardening

Build next:

- load testing
- failure injection
- DLQ replay tooling
- multi-AZ failover
- archival and retention policies
- operational dashboards

Outcome:

- production-grade operational readiness

## 22. Final Recommendation

For a strong capstone, do not try to ship all current microservices.

Build one coherent fulfillment architecture around:

- `fast order acceptance`
- `local inventory reservation`
- `warehouse task orchestration`
- `hub-and-spoke shipment milestones`
- `AI-assisted planning`

If executed well, this is enough to represent:

- Blinkit express fulfillment
- Amazon FC operations
- Delhivery/FedEx network movement
- AI-driven demand and replenishment planning

This is the target architecture to design against.
