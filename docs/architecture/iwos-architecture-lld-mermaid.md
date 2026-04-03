# IWOS Architecture LLD (Mermaid)

This document explains the internal working of the currently implemented execution path in the workspace on March 30, 2026.

Implemented services covered here:

- `order-intake-service`
- `order-orchestrator-service`
- `inventory-ledger-service`

Cross-cutting patterns already shared by these services:

- caller-supplied idempotency keys for write APIs
- `X-Request-Id` request correlation into MDC-backed logs
- PostgreSQL as transactional state
- Redis as the fast replay cache for idempotent commands
- outbox tables for downstream event publication

## 1. Order Intake Internal Components

```mermaid
flowchart LR
    classDef api fill:#fff3d6,stroke:#b7791f,color:#111111;
    classDef service fill:#dff3e4,stroke:#2f855a,color:#111111;
    classDef store fill:#e8f0fe,stroke:#2563eb,color:#111111;

    filter["RequestContextFilter<br/>X-Request-Id -> MDC"]:::api --> controller["OrderIntakeController"]:::api
    controller --> command["OrderIntakeCommandService"]:::service

    command --> hash["RequestHashService<br/>canonical SHA-256 hash"]:::service
    command --> cache["IdempotencyCache"]:::service
    command --> idRepo["IdempotencyRecordRepository"]:::service
    command --> orderRepo["OrderIntentRepository"]:::service
    command --> outboxRepo["OutboxEventRepository"]:::service
    command --> mapper["OrderIntentResponseMapper"]:::service

    cache --> redis["Redis<br/>24h TTL replay cache"]:::store
    idRepo --> idTable["idempotency_records"]:::store
    orderRepo --> orderTables["order_intents<br/>order_intent_items"]:::store
    outboxRepo --> outboxTable["outbox_events"]:::store

    publisher["OutboxEventPublisher<br/>@Scheduled every PT5S"]:::service --> outboxRepo
    publisher --> kafka["KafkaTemplate<br/>iwos.order-intake.accepted.v1"]:::store
```

### What This Service Owns

- fast synchronous order acceptance
- replay-safe deduplication across Redis and PostgreSQL
- normalized storage of order header, items, idempotency record, and accepted-event outbox row

## 2. Order Intake Request Flow

```mermaid
sequenceDiagram
    actor Caller
    participant Filter as RequestContextFilter
    participant Controller as OrderIntakeController
    participant Service as OrderIntakeCommandService
    participant Hash as RequestHashService
    participant Redis as IdempotencyCache
    participant IdTable as idempotency_records
    participant Orders as order_intents + order_intent_items
    participant Outbox as outbox_events
    participant Publisher as OutboxEventPublisher
    participant Kafka as Kafka topic

    Caller->>Filter: POST /api/v1/order-intents
    Filter->>Controller: attach or create X-Request-Id
    Controller->>Service: accept(idempotencyKey, request)
    Service->>Hash: hash(request)
    Service->>Redis: find(idempotencyKey)

    alt Redis replay hit
        Redis-->>Service: cached requestHash + response
        Service-->>Controller: replayed response
        Controller-->>Caller: 202 Accepted<br/>Idempotency-Replayed: true
    else Cache miss
        Service->>IdTable: findByIdempotencyKey(idempotencyKey)

        alt PostgreSQL replay hit
            IdTable-->>Service: persisted responseBody + requestHash
            Service->>Redis: cache response
            Service-->>Controller: replayed response
            Controller-->>Caller: 202 Accepted<br/>Idempotency-Replayed: true
        else New request
            Service->>Orders: insert order_intent + order_intent_items
            Service->>Outbox: insert PENDING order-intake.accepted.v1
            Service->>IdTable: insert idempotency record
            Service->>Redis: register afterCommit cache write
            Service-->>Controller: new accepted response
            Controller-->>Caller: 202 Accepted<br/>Idempotency-Replayed: false

            Publisher->>Outbox: poll oldest PENDING rows
            Publisher->>Kafka: publish aggregateId payload

            alt Kafka publish success
                Publisher->>Outbox: mark PUBLISHED
            else Kafka publish failure
                Publisher->>Outbox: mark FAILED
            end
        end
    end
```

## 3. Order Orchestrator Internal Components

```mermaid
flowchart LR
    classDef api fill:#fff3d6,stroke:#b7791f,color:#111111;
    classDef service fill:#dff3e4,stroke:#2f855a,color:#111111;
    classDef store fill:#e8f0fe,stroke:#2563eb,color:#111111;
    classDef caution fill:#fff1f2,stroke:#c2410c,color:#111111;

    kafkaConsumer["OrderIntakeEventConsumer<br/>@KafkaListener"]:::api --> workflow["OrderWorkflowProcessingService"]:::service
    workflowApi["OrderWorkflowController"]:::api --> workflow

    workflow --> sourceOutboxRepo["SourceOutboxEventRepository"]:::service
    workflow --> sourceOrderRepo["SourceOrderIntentRepository"]:::service
    workflow --> workflowRepo["OrderWorkflowRepository"]:::service
    workflow --> orchestrationOutboxRepo["OrderOrchestratorOutboxEventRepository"]:::service
    workflow --> inventoryClient["InventoryServiceClient"]:::service
    workflow --> mapper["OrderWorkflowResponseMapper"]:::service

    sourceOutboxRepo --> publicOutbox["public.outbox_events"]:::store
    sourceOrderRepo --> publicOrders["public.order_intents<br/>public.order_intent_items"]:::store
    workflowRepo --> workflowTables["order_orchestration.order_workflows<br/>order_orchestration.order_workflow_reservations"]:::store
    orchestrationOutboxRepo --> workflowOutbox["order_orchestration.order_orchestrator_outbox_events"]:::store
    inventoryClient --> inventoryApi["inventory-ledger-service HTTP API"]:::service

    workflowOutbox --> missingPublisher["no outbox publisher in current workspace"]:::caution
```

### What This Service Owns

- discovery of accepted orders to orchestrate
- workflow row creation and status tracking
- item-by-item inventory reservation
- compensation through release calls when a later item fails
- storage of downstream reservation references for each order item

## 4. Order Orchestration Flow

```mermaid
sequenceDiagram
    participant Kafka as iwos.order-intake.accepted.v1
    participant Consumer as OrderIntakeEventConsumer
    participant Workflow as OrderWorkflowProcessingService
    participant SourceDb as public order_intake tables
    participant OrchDb as order_orchestration schema
    participant Inventory as inventory-ledger-service

    Kafka->>Consumer: order-intake accepted event
    Consumer->>Workflow: processOrderIntent(orderIntentId)
    Workflow->>OrchDb: findByOrderIntentId / findBySourceOutboxEventId

    alt Workflow already exists
        OrchDb-->>Workflow: existing workflow
        Workflow-->>Consumer: existing response
        Consumer->>Kafka: acknowledge offset
    else New workflow
        Workflow->>SourceDb: load source outbox row
        Workflow->>SourceDb: load order_intent + items
        Workflow->>OrchDb: insert workflow(status=INVENTORY_RESERVATION_IN_PROGRESS)

        loop each order item in created_at order
            Workflow->>Inventory: POST /api/v1/reservations<br/>Idempotency-Key order-orchestrator:reserve:...
            Inventory-->>Workflow: reservationId + stock snapshot
        end

        alt all reservations succeed
            Workflow->>OrchDb: store reservation rows(status=RESERVED)
            Workflow->>OrchDb: update workflow(status=INVENTORY_RESERVED, completedAt)
            Workflow->>OrchDb: insert order-orchestrator.inventory-reserved.v1 outbox row
            Workflow-->>Consumer: success response
            Consumer->>Kafka: acknowledge offset
        else any reservation call fails
            loop each successful draft reservation
                Workflow->>Inventory: POST /api/v1/reservations/{reservationId}/release
                Inventory-->>Workflow: reservation released
            end
            Workflow->>OrchDb: store reservation rows(status=RELEASED)
            Workflow->>OrchDb: update workflow(status=INVENTORY_RESERVATION_FAILED, failureReason, completedAt)
            Workflow->>OrchDb: insert order-orchestrator.inventory-reservation-failed.v1 outbox row
            Workflow-->>Consumer: failure response
            Consumer->>Kafka: acknowledge offset
        end
    end
```

### Important Current-State Detail

The accepted Kafka event is not the full source of truth yet. The consumer uses the event only to locate the `orderIntentId`, then it rehydrates the full order from the order-intake tables in PostgreSQL.

## 5. Inventory Ledger Internal Components

```mermaid
flowchart LR
    classDef api fill:#fff3d6,stroke:#b7791f,color:#111111;
    classDef service fill:#dff3e4,stroke:#2f855a,color:#111111;
    classDef store fill:#e8f0fe,stroke:#2563eb,color:#111111;
    classDef caution fill:#fff1f2,stroke:#c2410c,color:#111111;

    stockApi["InventoryStockController"]:::api --> stockSvc["InventoryStockCommandService"]:::service
    reservationApi["InventoryReservationController"]:::api --> reservationSvc["InventoryReservationCommandService"]:::service
    querySvc["InventoryQueryService"]:::service

    stockSvc --> idem["InventoryCommandIdempotencyService"]:::service
    reservationSvc --> idem

    idem --> idemCache["CommandIdempotencyCache"]:::service
    idem --> idemRepo["InventoryCommandIdempotencyRecordRepository"]:::service
    idemCache --> redis["Redis<br/>24h TTL replay cache"]:::store
    idemRepo --> idemTable["inventory_command_idempotency_records"]:::store

    stockSvc --> mutation["InventoryStockMutationStore<br/>NamedParameterJdbcTemplate"]:::service
    reservationSvc --> mutation
    mutation --> stockTable["inventory_stock_items"]:::store

    stockSvc --> ledgerRepo["InventoryLedgerEntryRepository"]:::service
    reservationSvc --> ledgerRepo
    ledgerRepo --> ledgerTable["inventory_ledger_entries"]:::store

    reservationSvc --> reservationRepo["InventoryReservationRepository"]:::service
    reservationRepo --> reservationTable["inventory_reservations"]:::store

    stockSvc --> inventoryOutboxRepo["InventoryOutboxEventRepository"]:::service
    reservationSvc --> inventoryOutboxRepo
    inventoryOutboxRepo --> inventoryOutbox["inventory_outbox_events"]:::store

    querySvc --> stockTable
    querySvc --> reservationTable

    inventoryOutbox --> inventoryMissingPublisher["no outbox publisher in current workspace"]:::caution
```

### What This Service Owns

- atomic stock mutation per `(nodeId, sku)`
- reservation creation, confirmation, and release
- authoritative stock snapshot response generation
- ledger history for every adjustment and reservation transition
- idempotent write semantics across Redis and PostgreSQL

## 6. Inventory Reservation Command Flow

```mermaid
sequenceDiagram
    actor Orchestrator
    participant Api as InventoryReservationController
    participant Service as InventoryReservationCommandService
    participant Idem as InventoryCommandIdempotencyService
    participant Redis as CommandIdempotencyCache
    participant Mutation as InventoryStockMutationStore
    participant Stock as inventory_stock_items
    participant Reservations as inventory_reservations
    participant Ledger as inventory_ledger_entries
    participant Outbox as inventory_outbox_events

    Orchestrator->>Api: POST /api/v1/reservations
    Api->>Service: createReservation(idempotencyKey, request)
    Service->>Idem: resolveReplay(operation=CREATE_RESERVATION)
    Idem->>Redis: find(idempotencyKey)

    alt replay hit
        Redis-->>Idem: cached response body
        Idem-->>Service: replayed response
        Service-->>Api: cached response
        Api-->>Orchestrator: 202 Accepted
    else new command
        Service->>Mutation: reserve(nodeId, sku, quantity)
        Mutation->>Stock: UPDATE reserved_quantity += quantity<br/>WHERE available >= quantity
        Mutation-->>Service: current stock snapshot
        Service->>Reservations: insert RESERVED row with expiresAt = now + 15m
        Service->>Ledger: insert RESERVATION_CREATED
        Service->>Outbox: insert inventory.reservation-created.v1
        Service->>Idem: store successful response
        Idem->>Redis: cache after commit
        Service-->>Api: new reservation response
        Api-->>Orchestrator: 202 Accepted
    end
```

## 7. Inventory Reservation State Machine

```mermaid
stateDiagram-v2
    [*] --> RESERVED: createReservation
    RESERVED --> CONFIRMED: confirmReservation
    RESERVED --> RELEASED: releaseReservation
    CONFIRMED --> [*]
    RELEASED --> [*]
```

### Command Effects By Transition

| Transition | Stock Effect | Ledger Entry | Outbox Event |
|---|---|---|---|
| create reservation | `reserved_quantity +n` | `RESERVATION_CREATED` | `inventory.reservation-created.v1` |
| confirm reservation | `on_hand -n`, `reserved -n` | `RESERVATION_CONFIRMED` | `inventory.reservation-confirmed.v1` |
| release reservation | `reserved -n` | `RESERVATION_RELEASED` | `inventory.reservation-released.v1` |

## 8. Core Data Model Across The Implemented Slice

```mermaid
erDiagram
    ORDER_INTENTS {
        uuid order_intent_id PK
        string customer_id
        string channel
        string payment_mode
        string currency
        string status
        string request_hash
        timestamptz accepted_at
    }

    ORDER_INTENT_ITEMS {
        uuid order_intent_item_id PK
        uuid order_intent_id FK
        string sku
        int quantity
        timestamptz created_at
    }

    IDEMPOTENCY_RECORDS {
        uuid idempotency_record_id PK
        string idempotency_key UK
        string request_hash
        uuid order_intent_id
        int http_status
    }

    OUTBOX_EVENTS {
        uuid outbox_event_id PK
        string aggregate_type
        uuid aggregate_id
        string event_type
        string status
    }

    ORDER_WORKFLOWS {
        uuid workflow_id PK
        uuid order_intent_id UK
        uuid source_outbox_event_id UK
        string customer_id
        string fulfillment_node_id
        string status
        string failure_reason
    }

    ORDER_WORKFLOW_RESERVATIONS {
        uuid workflow_reservation_id PK
        uuid workflow_id FK
        uuid order_intent_item_id
        uuid inventory_reservation_id UK
        string node_id
        string sku
        int quantity
        string status
    }

    INVENTORY_STOCK_ITEMS {
        uuid stock_item_id PK
        string node_id
        string sku
        int on_hand_quantity
        int reserved_quantity
    }

    INVENTORY_RESERVATIONS {
        uuid reservation_id PK
        string order_reference
        string node_id
        string sku
        int quantity
        string status
        timestamptz expires_at
    }

    INVENTORY_LEDGER_ENTRIES {
        uuid ledger_entry_id PK
        string node_id
        string sku
        string entry_type
        int on_hand_delta
        int reserved_delta
        uuid reservation_id
    }

    INVENTORY_COMMAND_IDEMPOTENCY_RECORDS {
        uuid idempotency_record_id PK
        string idempotency_key UK
        string operation_type
        string request_hash
        uuid resource_id
        int http_status
    }

    INVENTORY_OUTBOX_EVENTS {
        uuid outbox_event_id PK
        string aggregate_type
        uuid aggregate_id
        string event_type
        string status
    }

    ORDER_INTENTS ||--o{ ORDER_INTENT_ITEMS : contains
    ORDER_INTENTS ||--o| IDEMPOTENCY_RECORDS : deduplicated_by
    ORDER_INTENTS ||--o| ORDER_WORKFLOWS : orchestrated_as
    OUTBOX_EVENTS ||--o| ORDER_WORKFLOWS : seeds
    ORDER_WORKFLOWS ||--o{ ORDER_WORKFLOW_RESERVATIONS : records
    ORDER_INTENT_ITEMS ||--o{ ORDER_WORKFLOW_RESERVATIONS : mirrors
    INVENTORY_STOCK_ITEMS ||--o{ INVENTORY_LEDGER_ENTRIES : mutated_by
    INVENTORY_RESERVATIONS ||--o{ INVENTORY_LEDGER_ENTRIES : explained_by
    ORDER_WORKFLOW_RESERVATIONS o|--|| INVENTORY_RESERVATIONS : references
```

### Schema Notes

- `order-intake-service` persists in the PostgreSQL `public` schema.
- `order-orchestrator-service` persists in the `order_orchestration` schema but still reads source data from `public`.
- `inventory-ledger-service` persists in the `inventory_ledger` schema.
- Several references are logical rather than DB-enforced foreign keys, especially around outbox rows and polymorphic idempotency `resource_id` values.
