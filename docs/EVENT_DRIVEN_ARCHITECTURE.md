# Event-Driven Architecture - IWOS

## 🏗️ Architecture Pattern: **Event-Driven Choreography**

---

## 📊 Architecture Overview

```
┌────────────────────────────────────────────────────────────────┐
│                    CLIENT (React Frontend)                      │
└────────────────────────┬───────────────────────────────────────┘
                         │ HTTP REST
                         ▼
┌────────────────────────────────────────────────────────────────┐
│                      Order Service                              │
│  - Create Order                                                 │
│  - Validate Request                                            │
│  - Save to DB                                                  │
│  - 📤 PUBLISH: "order.created" event to Kafka                  │
└────────────────────────┬───────────────────────────────────────┘
                         │
                         ▼
┌────────────────────────────────────────────────────────────────┐
│                  KAFKA MESSAGE BUS                              │
│  Topic: "order.events"                                         │
│  Event: { orderId, orderNumber, warehouseId, items... }        │
└───────┬──────────────┬─────────────┬──────────────────────────┘
        │              │             │
        ▼              ▼             ▼
┌─────────────┐  ┌─────────────┐  ┌─────────────┐
│ Inventory   │  │ Warehouse   │  │Notification │
│  Service    │  │   Service   │  │  Service    │
│             │  │             │  │             │
│ 📥 LISTENS  │  │ 📥 LISTENS  │  │ 📥 LISTENS  │
│             │  │             │  │             │
│ Actions:    │  │ Actions:    │  │ Actions:    │
│ - Reserve   │  │ - Assign    │  │ - Send      │
│   stock     │  │   zone      │  │   email     │
│ - Check     │  │ - Optimize  │  │ - Push      │
│   levels    │  │   route     │  │   notify    │
│             │  │             │  │             │
│ 📤 PUBLISH: │  │ 📤 PUBLISH: │  │             │
│ "inventory. │  │ "order.     │  │             │
│  reserved"  │  │  assigned"  │  │             │
└─────────────┘  └─────────────┘  └─────────────┘
```

---

## 🎭 Pattern Type: **Choreography** (not Orchestration)

### What is Choreography?

**Choreography** is like a dance where each dancer knows their moves and reacts to music and other dancers **without a conductor**:

- ✅ **Decentralized control** - Each service decides what to do
- ✅ **Event-driven** - Services react to events
- ✅ **Loose coupling** - Services don't call each other directly
- ✅ **Scalable** - No single bottleneck

### Choreography vs Orchestration

| Aspect | Choreography (IWOS) | Orchestration |
|--------|---------------------|---------------|
| **Control** | Decentralized | Centralized (Saga coordinator) |
| **Communication** | Events (pub/sub) | Direct calls (API) |
| **Coupling** | Loose | Tight |
| **Adding services** | Easy (subscribe to events) | Hard (update orchestrator) |
| **Failure handling** | Each service handles own | Orchestrator handles all |
| **Example** | Order created → Inventory auto-reserves | Saga: "Inventory, reserve!" |

**We chose Choreography because:**
1. Better scalability (no central bottleneck)
2. Easier to add new features
3. Services remain independent
4. Fits warehouse operations (many parallel processes)

---

## 🔄 Event Flow Example: Create Order

### Step-by-Step Flow

```
┌─────────────────────────────────────────────────────────────────┐
│ 1. Customer creates order via Frontend                          │
└────────────────────┬────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────────┐
│ 2. Order Service                                                │
│    - Validates order data                                       │
│    - Saves order to database (status: PENDING)                  │
│    - Publishes event to Kafka:                                  │
│      {                                                          │
│        "eventType": "order.created",                            │
│        "orderId": 12345,                                        │
│        "orderNumber": "ORD-2025-12345",                         │
│        "items": [{"sku": "SKU-001", "qty": 5}],                │
│        "warehouseId": 1                                         │
│      }                                                          │
└────────────────────┬────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────────┐
│ 3. KAFKA distributes event to all subscribers                   │
└───────┬──────────────┬─────────────┬─────────────────────────────┘
        │              │             │
        ▼              ▼             ▼
┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│ Inventory    │ │ Warehouse    │ │ Notification │
│ Service      │ │ Service      │ │ Service      │
│              │ │              │ │              │
│ 4a. Checks   │ │ 4b. Assigns  │ │ 4c. Sends    │
│     stock    │ │     to zone  │ │     email    │
│     levels   │ │     A1       │ │     to       │
│              │ │              │ │     customer │
│ If OK:       │ │              │ │              │
│ - Reserve 5  │ │ Publishes:   │ │              │
│   units      │ │ "order.      │ │              │
│              │ │  assigned"   │ │              │
│ Publishes:   │ │              │ │              │
│ "inventory.  │ │              │ │              │
│  reserved"   │ │              │ │              │
│              │ │              │ │              │
│ If NOT OK:   │ │              │ │              │
│ Publishes:   │ │              │ │              │
│ "inventory.  │ │              │ │              │
│  insufficient│ │              │ │              │
└──────────────┘ └──────────────┘ └──────────────┘
```

---

## 📡 Event Types in IWOS

### 1. Order Events (Topic: `order.events`)

| Event Type | Published By | Consumed By | Purpose |
|------------|--------------|-------------|---------|
| `order.created` | Order Service | Inventory, Warehouse, Notification | New order needs processing |
| `order.confirmed` | Order Service | Warehouse, Notification | Inventory reserved successfully |
| `order.cancelled` | Order Service | Inventory, Notification | Release reserved inventory |
| `order.shipped` | Order Service | Notification | Inform customer |
| `order.assigned` | Warehouse Service | Order Service, Notification | Order assigned to worker/zone |

### 2. Inventory Events (Topic: `inventory.events`)

| Event Type | Published By | Consumed By | Purpose |
|------------|--------------|-------------|---------|
| `inventory.reserved` | Inventory Service | Order, Warehouse | Stock reserved for order |
| `inventory.released` | Inventory Service | Order | Reserved stock released |
| `inventory.low_stock` | Inventory Service | Warehouse, Notification | Reorder alert |
| `inventory.adjusted` | Inventory Service | Order, Warehouse | Manual stock adjustment |

### 3. Warehouse Events (Topic: `warehouse.events`)

| Event Type | Published By | Consumed By | Purpose |
|------------|--------------|-------------|---------|
| `zone.capacity.updated` | Warehouse Service | Inventory | Zone capacity changed |
| `order.assigned` | Warehouse Service | Order, Notification | Order assigned to zone |

---

## 🛠️ Implementation Details

### Event Structure (JSON)

```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440000",
  "eventType": "order.created",
  "timestamp": "2025-11-05T10:30:00Z",
  "source": "order-service",
  "correlationId": "client-request-789",
  "aggregateId": "order-12345",
  "payload": {
    "orderId": 12345,
    "orderNumber": "ORD-2025-12345",
    "customerId": 789,
    "warehouseId": 1,
    "items": [
      {
        "sku": "SKU-001",
        "quantity": 5,
        "unitPrice": 29.99
      }
    ],
    "totalAmount": 149.95,
    "status": "PENDING"
  }
}
```

### Kafka Configuration

```yaml
# application.yml
spring:
  kafka:
    bootstrap-servers: localhost:9093
    producer:
      key-serializer: StringSerializer
      value-serializer: JsonSerializer
      acks: all  # Wait for all replicas
      retries: 3
    consumer:
      key-deserializer: StringDeserializer
      value-deserializer: JsonDeserializer
      group-id: ${spring.application.name}
      auto-offset-reset: earliest
```

### Publishing Events (Order Service)

```java
@Service
@RequiredArgsConstructor
public class OrderService {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public Order createOrder(CreateOrderRequest request) {
        // 1. Save order to database
        Order order = orderRepository.save(buildOrder(request));

        // 2. Publish event (fire and forget - async)
        OrderEvent event = OrderEvent.orderCreated(
            order.getId(),
            order.getOrderNumber(),
            order.getWarehouseId(),
            order.getTotalAmount(),
            order.getTotalItems()
        );

        kafkaTemplate.send("order.events", order.getId().toString(), event);

        // 3. Return immediately (don't wait for consumers)
        return order;
    }
}
```

### Consuming Events (Inventory Service)

```java
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final InventoryService inventoryService;

    @KafkaListener(
        topics = "order.events",
        groupId = "inventory-service"
    )
    public void handleOrderEvent(OrderEvent event) {
        if ("order.created".equals(event.getEventType())) {
            // Reserve inventory
            inventoryService.reserveForOrder(
                event.getOrderId(),
                event.getItems()
            );

            // Publish success event
            publishInventoryReservedEvent(event);
        }
    }
}
```

---

## ⚡ Benefits of This Architecture

### 1. **Scalability**
- Services can be scaled independently
- No single bottleneck
- Kafka handles millions of events/sec

### 2. **Loose Coupling**
- Services don't know about each other
- Can add new services without modifying existing ones
- Easy to test in isolation

### 3. **Resilience**
- If one service is down, others continue working
- Events are persisted in Kafka (no data loss)
- Can replay events for recovery

### 4. **Flexibility**
- Easy to add new features (just subscribe to events)
- Example: Add ML service to predict demand → just listen to order.created

### 5. **Async Processing**
- Order Service returns immediately
- Inventory reservation happens in background
- Better user experience (faster response)

---

## 🚨 Challenges & Solutions

### Challenge 1: **Event Ordering**

**Problem**: Events might arrive out of order

**Solution**:
- Use same key for related events (orderId)
- Kafka guarantees ordering within partition
- Events with same orderId go to same partition

### Challenge 2: **Duplicate Events**

**Problem**: Consumer might receive same event twice

**Solution**:
- Make consumers idempotent
- Check if order already processed
- Use event IDs for deduplication

```java
@Transactional
public void handleOrderCreated(OrderEvent event) {
    // Idempotent check
    if (processedEvents.contains(event.getEventId())) {
        log.warn("Event already processed: {}", event.getEventId());
        return;
    }

    // Process event
    reserveInventory(event);

    // Mark as processed
    processedEvents.add(event.getEventId());
}
```

### Challenge 3: **Distributed Transaction**

**Problem**: What if inventory reservation fails after order is created?

**Solution**: Implement Saga pattern with compensating transactions

```
Order Created → Inventory Reserved → Success ✅

Order Created → Inventory Failed → Publish order.failed
                                 → Compensate (delete order) ❌
```

---

## 🎯 Current Implementation Status

### ✅ Completed
- Kafka infrastructure (Docker Compose)
- Service configurations
- Event classes (OrderEvent, DomainEvent)
- Event publisher (OrderEventPublisher)
- Event consumer stub (OrderEventConsumer)

### 🚧 To Be Implemented
- Actual event publishing in Order Service
- Inventory reservation logic in consumer
- Saga pattern for distributed transactions
- Event replay for recovery
- Dead letter queue for failed events

---

## 📚 Further Reading

### Books
- "Building Event-Driven Microservices" - Adam Bellemare
- "Designing Data-Intensive Applications" - Martin Kleppmann

### Articles
- [Event-Driven Architecture (AWS)](https://aws.amazon.com/event-driven-architecture/)
- [Kafka Documentation](https://kafka.apache.org/documentation/)
- [Choreography vs Orchestration](https://microservices.io/patterns/data/saga.html)

---

## 🏁 Summary

**IWOS uses Event-Driven Choreography Architecture where:**

1. Services communicate via **events** (not direct calls)
2. **Kafka** is the message bus
3. Each service **reacts independently** to events
4. No central orchestrator (decentralized)
5. Loose coupling, high scalability

**Example Flow:**
```
Order Created → Event Published → Kafka → All Services Listen → React Independently
```

This is the same architecture used by:
- **Amazon** (order fulfillment)
- **Uber** (ride matching)
- **Netflix** (content recommendations)
- **Airbnb** (booking processing)

---

**Need to see it in action?** Run `./start-all.sh` and check Kafka UI at http://localhost:8090
