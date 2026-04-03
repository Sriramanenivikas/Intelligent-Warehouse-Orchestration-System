# IWOS Architecture HLD (Mermaid)

This document reflects the repository workspace on March 30, 2026.

It separates two things that currently coexist in the repo:

- the `target platform architecture` described by the service catalog and architecture docs
- the `implemented execution slice` that already has runnable Java code

## 1. Coverage Summary

| Scope | Status | Notes |
|---|---|---|
| `order-intake-service` | implemented | synchronous order acceptance, idempotency, outbox publisher |
| `order-orchestrator-service` | implemented | Kafka consumer, workflow persistence, inventory reservation orchestration |
| `inventory-ledger-service` | implemented | stock adjustment, reservation lifecycle, idempotent command handling |
| remaining service catalog entries | scaffolded | contracts, deployment files, and runtime configs exist but no Java execution path yet |
| platform modules | partial | `common-kernel`, `kafka-starter`, `observability-starter`, `service-testkit`, Kong, observability stack, Terraform skeletons |

## 2. Full Project Service Landscape

```mermaid
flowchart TB
    classDef actor fill:#fff3d6,stroke:#b7791f,color:#111111;
    classDef implemented fill:#dff3e4,stroke:#2f855a,color:#111111;
    classDef scaffolded fill:#f3f4f6,stroke:#6b7280,color:#111111;
    classDef infra fill:#e8f0fe,stroke:#2563eb,color:#111111;

    customer["Customer Channels<br/>App | Web | Partner"]:::actor
    ops["Ops, Support, Warehouse Clients"]:::actor
    edge["Kong Edge / Public API Gateway"]:::scaffolded

    customer --> edge
    ops --> edge

    subgraph commerce["Commerce Domain"]
        direction LR
        identity["identity-service"]:::scaffolded
        catalog["catalog-service"]:::scaffolded
        pricing["pricing-service"]:::scaffolded
        cart["cart-service"]:::scaffolded
        search["search-service"]:::scaffolded
        orderIntake["order-intake-service"]:::implemented
        orderOrchestrator["order-orchestrator-service"]:::implemented
        promise["promise-allocation-service"]:::scaffolded
        payment["payment-service"]:::scaffolded
    end

    subgraph fulfillment["Fulfillment Domain"]
        direction LR
        inventory["inventory-ledger-service"]:::implemented
        nodes["node-registry-service"]:::scaffolded
        warehouse["warehouse-orchestrator-service"]:::scaffolded
        tasks["task-execution-service"]:::scaffolded
        returns["returns-service"]:::scaffolded
    end

    subgraph network["Network Domain"]
        direction LR
        shipment["shipment-network-service"]:::scaffolded
        scan["scan-event-service"]:::scaffolded
        notification["notification-service"]:::scaffolded
    end

    subgraph intelligence["Intelligence Domain"]
        direction LR
        forecasting["forecasting-planning-service"]:::scaffolded
        features["feature-platform-service"]:::scaffolded
        controlTower["control-tower-service"]:::scaffolded
    end

    subgraph platform["Shared Platform"]
        direction LR
        kafka["Kafka / MSK Event Backbone"]:::infra
        aurora["Aurora PostgreSQL"]:::infra
        redis["Redis"]:::infra
        dynamo["DynamoDB"]:::infra
        openSearch["OpenSearch"]:::infra
        s3["S3 Data Lake"]:::infra
        observability["Grafana + Prometheus + Loki + Jaeger + OTel"]:::infra
    end

    edge --> identity
    edge --> catalog
    edge --> cart
    edge --> search
    edge --> orderIntake
    edge --> payment

    orderIntake --> orderOrchestrator
    orderOrchestrator --> promise
    orderOrchestrator --> inventory
    promise --> nodes
    inventory --> warehouse
    warehouse --> tasks
    warehouse --> shipment
    shipment --> scan
    scan --> notification
    returns --> inventory

    orderIntake -. accepted events .-> kafka
    orderOrchestrator -. orchestration events .-> kafka
    inventory -. reservation and stock events .-> kafka
    warehouse -. task events .-> kafka
    shipment -. network events .-> kafka
    scan -. milestone events .-> kafka
    kafka -. feeds .-> notification
    kafka -. read models .-> controlTower
    kafka -. planning inputs .-> forecasting
    forecasting --> features

    orderIntake --> aurora
    orderIntake --> redis
    orderOrchestrator --> aurora
    inventory --> aurora
    inventory --> redis
    scan --> dynamo
    search --> openSearch
    forecasting --> s3
    features --> redis

    orderIntake -. telemetry .-> observability
    orderOrchestrator -. telemetry .-> observability
    inventory -. telemetry .-> observability
```

### Reading Notes

- The service map in the repository is broad and intentionally production-shaped.
- Only the three green services currently execute business logic.
- The rest of the boxes still matter architecturally because their contracts, deployment scaffolding, and domain boundaries are already present in the repo.

## 3. Target Regional Cell Runtime

```mermaid
flowchart LR
    classDef actor fill:#fff3d6,stroke:#b7791f,color:#111111;
    classDef svc fill:#dff3e4,stroke:#2f855a,color:#111111;
    classDef planned fill:#f3f4f6,stroke:#6b7280,color:#111111;
    classDef infra fill:#e8f0fe,stroke:#2563eb,color:#111111;

    client["Customer / Partner Traffic"]:::actor --> kong["Kong Gateway<br/>per regional cell"]:::planned

    subgraph eks["EKS Application Plane"]
        direction TB
        intake["Order Intake"]:::svc
        orchestration["Order Orchestrator"]:::svc
        ledger["Inventory Ledger"]:::svc
        other["Promise, Payment, Warehouse, Shipment,<br/>Scan, Notification, Intelligence Services"]:::planned
    end

    subgraph data["Regional Data Plane"]
        direction TB
        pg["Aurora PostgreSQL"]:::infra
        cache["Redis"]:::infra
        bus["Kafka / MSK"]:::infra
        kv["DynamoDB"]:::infra
        lake["S3 Data Lake"]:::infra
        searchIndex["OpenSearch"]:::infra
    end

    subgraph opsPlane["Delivery and Observability"]
        direction TB
        cicd["GitHub Actions + Helm"]:::infra
        telemetry["Grafana / Prometheus / Loki / Jaeger / OTel"]:::infra
    end

    kong --> intake
    intake --> orchestration
    orchestration --> ledger
    orchestration --> other
    ledger --> other

    intake --> pg
    intake --> cache
    intake -. publishes .-> bus

    orchestration --> pg
    orchestration -. publishes .-> bus

    ledger --> pg
    ledger --> cache
    ledger -. publishes .-> bus

    other --> pg
    other --> kv
    other --> searchIndex
    other --> lake
    other -. publishes and consumes .-> bus

    intake -. telemetry .-> telemetry
    orchestration -. telemetry .-> telemetry
    ledger -. telemetry .-> telemetry
    other -. telemetry .-> telemetry

    cicd --> intake
    cicd --> orchestration
    cicd --> ledger
    cicd --> other
```

### Reading Notes

- The canonical architecture is `fast accept + async orchestration`, not one synchronous end-to-end checkout.
- Regional cell boundaries keep hot-path state, Kafka, and fulfillment operations local.
- The repo already contains the AWS-facing deployment language for this model: Kong manifests, Helm charts, Terraform modules, and observability stack definitions.

## 4. Current Implemented Runtime Slice

```mermaid
flowchart LR
    classDef actor fill:#fff3d6,stroke:#b7791f,color:#111111;
    classDef svc fill:#dff3e4,stroke:#2f855a,color:#111111;
    classDef store fill:#e8f0fe,stroke:#2563eb,color:#111111;
    classDef caution fill:#fff1f2,stroke:#c2410c,color:#111111;

    caller["Checkout Caller"]:::actor --> intake["order-intake-service<br/>HTTP :8081"]:::svc

    intake --> intakeDb["PostgreSQL public schema<br/>order_intents<br/>order_intent_items<br/>idempotency_records<br/>outbox_events"]:::store
    intake --> intakeRedis["Redis<br/>order-intake:idempotency:*"]:::store
    intake -. scheduled outbox publish .-> intakeTopic["Kafka topic<br/>iwos.order-intake.accepted.v1"]:::store

    intakeTopic --> orchestrator["order-orchestrator-service<br/>HTTP :8083 + Kafka consumer"]:::svc
    orchestrator -->|rehydrates order intent and source event| intakeDb
    orchestrator --> orchestrationDb["PostgreSQL order_orchestration schema<br/>order_workflows<br/>order_workflow_reservations<br/>order_orchestrator_outbox_events"]:::store
    orchestrator --> inventory["inventory-ledger-service<br/>HTTP :8082"]:::svc

    inventory --> inventoryDb["PostgreSQL inventory_ledger schema<br/>inventory_stock_items<br/>inventory_reservations<br/>inventory_ledger_entries<br/>inventory_outbox_events"]:::store
    inventory --> inventoryRedis["Redis<br/>inventory-ledger:idempotency:*"]:::store

    orchestrator -. writes PENDING orchestration events .-> orchestratorOutbox["order_orchestrator_outbox_events<br/>publisher not present yet"]:::caution
    inventory -. writes PENDING inventory events .-> inventoryOutbox["inventory_outbox_events<br/>publisher not present yet"]:::caution
```

### Current-State Findings

- `order-intake-service` now implements a real transactional outbox publisher to Kafka.
- `order-orchestrator-service` consumes Kafka, but it still rehydrates source order data by reading the `public` order-intake tables directly from PostgreSQL.
- `order-orchestrator-service` and `inventory-ledger-service` both persist outbox rows, but no publisher for those outbox tables exists in the current workspace.
- This means the current runtime is already event-driven at ingress, but still hybrid and DB-coupled downstream.
