package com.iwos.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Order Event Consumer (in Inventory Service)
 *
 * Architecture Pattern: Event-Driven Choreography
 *
 * How Choreography Works:
 * ========================
 *
 * 1. Order Service publishes "order.created" event
 *    ↓
 * 2. Inventory Service (THIS CLASS) listens and reacts:
 *    - Reserves inventory automatically
 *    - Publishes "inventory.reserved" event
 *    ↓
 * 3. Warehouse Service listens to "inventory.reserved":
 *    - Assigns order to optimal zone
 *    - Publishes "order.assigned" event
 *    ↓
 * 4. Notification Service listens to all events:
 *    - Sends confirmation email to customer
 *
 * Key Points:
 * - No central orchestrator
 * - Each service decides what to do when it hears an event
 * - Services communicate via events, not direct calls
 * - Easy to add new services (just subscribe to events)
 *
 * Choreography vs Orchestration:
 * ==============================
 *
 * CHOREOGRAPHY (what we use):
 * - Decentralized control
 * - Services react to events independently
 * - Like a dance - each dancer knows their moves
 * - Example: Order created → Inventory auto-reserves
 *
 * ORCHESTRATION (alternative):
 * - Centralized control
 * - Central service tells others what to do
 * - Like a conductor directing an orchestra
 * - Example: Saga Orchestrator: "Inventory, reserve stock!"
 *
 * We chose CHOREOGRAPHY because:
 * - Better scalability (no bottleneck)
 * - Easier to add new services
 * - Services are more independent
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    // Inject services here (InventoryService, etc.)

    /**
     * Listen to order events and react accordingly
     *
     * Kafka Concepts:
     * - Topic: "order.events" (where events are published)
     * - Consumer Group: "inventory-service" (each service has unique group)
     * - Partitions: Events with same orderId go to same partition (ordering preserved)
     */
    @KafkaListener(
        topics = "order.events",
        groupId = "inventory-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleOrderEvent(String eventJson) {
        log.info("📥 Received order event in Inventory Service");

        // Parse event and handle based on type
        // Example logic:
        /*
        OrderEvent event = parseEvent(eventJson);

        switch (event.getEventType()) {
            case "order.created":
                handleOrderCreated(event);
                break;
            case "order.cancelled":
                handleOrderCancelled(event);
                break;
        }
        */

        log.info("✅ Order event processed by Inventory Service");
    }

    /**
     * Handle order created event
     *
     * Business Logic:
     * 1. Check if all items are in stock
     * 2. If yes: Reserve inventory
     * 3. Publish "inventory.reserved" event
     * 4. If no: Publish "inventory.insufficient" event
     */
    private void handleOrderCreated(OrderEvent event) {
        log.info("🛒 Processing order creation: {}", event.getOrderNumber());

        // TODO: Implement inventory reservation logic
        // 1. Get order items from Order Service
        // 2. Check stock availability
        // 3. Reserve stock (increase reserved_quantity)
        // 4. Publish success/failure event

        log.info("✅ Inventory reserved for order: {}", event.getOrderNumber());

        // Publish event to notify other services
        // publishInventoryReservedEvent(event);
    }

    /**
     * Handle order cancelled event
     *
     * Business Logic:
     * 1. Release reserved inventory
     * 2. Update inventory_transactions table
     * 3. Publish "inventory.released" event
     */
    private void handleOrderCancelled(OrderEvent event) {
        log.info("❌ Processing order cancellation: {}", event.getOrderNumber());

        // TODO: Implement inventory release logic
        // 1. Find reserved inventory for this order
        // 2. Decrease reserved_quantity
        // 3. Increase available_quantity
        // 4. Log transaction

        log.info("✅ Inventory released for order: {}", event.getOrderNumber());
    }
}

/**
 * Order Event DTO (copy from Order Service)
 */
class OrderEvent {
    private String eventType;
    private String orderNumber;
    private Long orderId;
    private Long warehouseId;

    public String getEventType() { return eventType; }
    public String getOrderNumber() { return orderNumber; }
}
