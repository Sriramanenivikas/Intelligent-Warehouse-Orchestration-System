# 📨 Event-Driven Architecture

## 1. Kafka Topic Map

```mermaid
graph TB
    subgraph "Producers"
        ORD_P["Order Service"]
        CAT_P["Catalog Service"]
        PAY_P["Payment Service"]
        INV_P["Inventory Service"]
        DSP_P["Dispatch Service"]
        TRK_P["Tracking Service"]
        CART_P["Cart Service"]
        DS_P["Dark Store Service"]
    end

    subgraph "Kafka Topics"
        T1["order.events<br>P:3 R:3"]
        T2["catalog.events<br>P:3 R:1"]
        T3["payment.events<br>P:3 R:3"]
        T4["inventory.events<br>P:3 R:3"]
        T5["dispatch.events<br>P:3 R:1"]
        T6["tracking.gps<br>P:6 R:1"]
        T7["notification.commands<br>P:3 R:1"]
        T8["fraud.alerts<br>P:3 R:3"]
        T9["darkstore.events<br>P:3 R:1"]
    end

    subgraph "Consumers"
        INV_C["Inventory Service"]
        PAY_C["Payment Service"]
        SRCH_C["Search Service"]
        NOTIF_C["Notification Service"]
        FRAUD_C["Fraud Detection"]
        DSP_C["Dispatch Service"]
        DS_C["Dark Store Service"]
        PRED_C["Stock Predictor"]
        RECOM_C["Recommendation"]
    end

    ORD_P --> T1
    CAT_P --> T2
    PAY_P --> T3
    INV_P --> T4
    DSP_P --> T5
    TRK_P --> T6
    DS_P --> T9

    T1 --> INV_C & PAY_C & NOTIF_C & FRAUD_C & DSP_C & RECOM_C
    T2 --> SRCH_C & DS_C
    T3 --> NOTIF_C
    T4 --> PRED_C & DS_C
    T5 --> NOTIF_C
    T8 --> NOTIF_C
```

## 2. Event Schemas

### Order Events

| Event | Topic | Key | Trigger | Consumers |
|-------|-------|-----|---------|-----------|
| `OrderCreatedEvent` | order.events | orderId | New order placed | Inventory, Fraud, Notification |
| `OrderConfirmedEvent` | order.events | orderId | Payment successful | Dispatch, Notification |
| `OrderShippedEvent` | order.events | orderId | Picked up by delivery | Tracking, Notification |
| `OrderDeliveredEvent` | order.events | orderId | Delivered | Notification, Review (prompt) |
| `OrderCancelledEvent` | order.events | orderId | User/system cancel | Inventory (release), Payment (refund) |

### Catalog Events

| Event | Topic | Key | Trigger | Consumers |
|-------|-------|-----|---------|-----------|
| `ProductCreatedEvent` | catalog.events | productId | New product listed | Search (index), DarkStore |
| `ProductUpdatedEvent` | catalog.events | productId | Price/detail changed | Search (re-index) |
| `ProductDeletedEvent` | catalog.events | productId | Product delisted | Search (remove) |

### Payment Events

| Event | Topic | Key | Trigger | Consumers |
|-------|-------|-----|---------|-----------|
| `PaymentInitiatedEvent` | payment.events | paymentId | Checkout started | Fraud (scoring) |
| `PaymentCompletedEvent` | payment.events | paymentId | Gateway confirms | Order (confirm), Notification |
| `PaymentFailedEvent` | payment.events | paymentId | Gateway rejects | Order (cancel), Notification |
| `RefundCompletedEvent` | payment.events | paymentId | Refund processed | Notification |

### Inventory Events

| Event | Topic | Key | Trigger | Consumers |
|-------|-------|-----|---------|-----------|
| `StockReservedEvent` | inventory.events | skuCode | Order reservation | Order saga |
| `StockReleasedEvent` | inventory.events | skuCode | Order cancelled | Dark Store |
| `StockLowEvent` | inventory.events | skuCode | Below reorder level | Predictor, Dark Store |
| `StockReplenishedEvent` | inventory.events | skuCode | New stock received | Dark Store |

## 3. Choreography vs Orchestration

```mermaid
graph TB
    subgraph "Choreography (Kafka Events)"
        direction LR
        A1["Catalog creates product"] -->|"product.created"| B1["Search indexes product"]
        A1 -->|"product.created"| C1["Dark Store syncs"]
        D1["Order delivered"] -->|"order.delivered"| E1["Review prompt sent"]
        D1 -->|"order.delivered"| F1["Recommendation updated"]
    end

    subgraph "Orchestration (Temporal Saga)"
        direction LR
        S1["Start Order Saga"] --> S2["Reserve Inventory"]
        S2 -->|"success"| S3["Process Payment"]
        S3 -->|"success"| S4["Assign Delivery"]
        S4 -->|"success"| S5["Saga Complete"]
        S2 -->|"fail"| S2C["Cancel Order"]
        S3 -->|"fail"| S3C["Release Inventory"]
        S4 -->|"fail"| S4C["Refund + Release"]
    end
```

| Aspect | Choreography | Orchestration |
|--------|-------------|---------------|
| **Use when** | Independent reactions | Coordinated multi-step transactions |
| **Coupling** | Loose | Tighter (via orchestrator) |
| **Visibility** | Harder to trace | Full workflow visibility |
| **Compensation** | Complex | Built-in with Temporal |
| **Example** | Catalog → Search sync | Order → Reserve → Pay → Ship |

## 4. Guaranteed Delivery & Error Handling

```
Producer → Kafka (acks=all) → Consumer Group → Process → Commit offset

On failure:
  → Retry (exponential backoff, max 3 attempts)
  → Dead Letter Queue (DLQ topic: {topic}.dlq)
  → Alert via Notification service
  → Manual intervention dashboard
```

**Consumer Group IDs:**
- `order-service` — for payment events
- `inventory-service` — for order events
- `search-service` — for catalog events
- `notification-service` — for all notification-worthy events
- `fraud-service` — for transaction events
- `darkstore-service` — for inventory + catalog events
