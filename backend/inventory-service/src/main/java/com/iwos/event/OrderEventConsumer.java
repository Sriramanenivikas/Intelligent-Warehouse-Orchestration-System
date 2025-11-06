package com.iwos.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwos.dto.*;
import com.iwos.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

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

    private final InventoryService inventoryService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

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
        groupId = "inventory-service"
    )
    public void handleOrderEvent(String eventJson) {
        log.info("Received order event in Inventory Service");

        try {
            // Parse event
            OrderEvent event = objectMapper.readValue(eventJson, OrderEvent.class);
            log.info("Parsed event type: {}, Order ID: {}",
                    event.getEventType(),
                    event.getOrder() != null ? event.getOrder().getOrderId() : "N/A");

            // Handle based on event type
            switch (event.getEventType()) {
                case "ORDER_CREATED":
                    handleOrderCreated(event);
                    break;
                case "ORDER_CANCELLED":
                    handleOrderCancelled(event);
                    break;
                case "ORDER_CONFIRMED":
                    handleOrderConfirmed(event);
                    break;
                default:
                    log.info("Ignoring event type: {}", event.getEventType());
            }

        } catch (Exception e) {
            log.error("Error processing order event", e);
            // In production, you might want to send to a dead letter queue
        }
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
        OrderEvent.OrderData order = event.getOrder();
        log.info("Processing order creation: Order #{} with {} items",
                order.getOrderNumber(), order.getItems() != null ? order.getItems().size() : 0);

        try {
            // Build reservation request from order data
            ReserveInventoryRequest reservationRequest = ReserveInventoryRequest.builder()
                    .orderId(order.getOrderId())
                    .warehouseId(order.getWarehouseId())
                    .items(order.getItems().stream()
                            .map(item -> ReserveInventoryRequest.ReservationItem.builder()
                                    .skuId(item.getSkuId())
                                    .quantity(item.getQuantity())
                                    .build())
                            .collect(Collectors.toList()))
                    .build();

            // Reserve inventory
            ReservationResponse response = inventoryService.reserveInventory(reservationRequest);

            if (response.getSuccess()) {
                log.info("Successfully reserved inventory for order {}", order.getOrderNumber());
                publishInventoryEvent(InventoryEventType.INVENTORY_RESERVED, order, response);
            } else {
                log.warn("Failed to reserve inventory for order {}: {}",
                        order.getOrderNumber(), response.getErrors());
                publishInventoryEvent(InventoryEventType.INVENTORY_INSUFFICIENT, order, response);
            }

        } catch (Exception e) {
            log.error("Error reserving inventory for order {}", order.getOrderNumber(), e);
            publishInventoryEvent(InventoryEventType.INVENTORY_INSUFFICIENT, order, null);
        }
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
        OrderEvent.OrderData order = event.getOrder();
        log.info("Processing order cancellation: Order #{}", order.getOrderNumber());

        try {
            // Build release request from order data
            ReleaseInventoryRequest releaseRequest = ReleaseInventoryRequest.builder()
                    .orderId(order.getOrderId())
                    .warehouseId(order.getWarehouseId())
                    .items(order.getItems().stream()
                            .map(item -> ReleaseInventoryRequest.ReleaseItem.builder()
                                    .skuId(item.getSkuId())
                                    .quantity(item.getQuantity())
                                    .build())
                            .collect(Collectors.toList()))
                    .reason("Order cancelled")
                    .build();

            // Release inventory
            ReservationResponse response = inventoryService.releaseInventory(releaseRequest);

            if (response.getSuccess()) {
                log.info("Successfully released inventory for order {}", order.getOrderNumber());
                publishInventoryEvent(InventoryEventType.INVENTORY_RELEASED, order, response);
            } else {
                log.warn("Failed to release inventory for order {}: {}",
                        order.getOrderNumber(), response.getErrors());
            }

        } catch (Exception e) {
            log.error("Error releasing inventory for order {}", order.getOrderNumber(), e);
        }
    }

    /**
     * Handle order confirmed event
     * This could be used for additional business logic like updating metrics
     */
    private void handleOrderConfirmed(OrderEvent event) {
        OrderEvent.OrderData order = event.getOrder();
        log.info("Order confirmed: Order #{} - Inventory remains reserved", order.getOrderNumber());
        // Inventory stays reserved until order is fulfilled or cancelled
    }

    /**
     * Publish inventory event to Kafka
     *
     * @param eventType Type of inventory event
     * @param order Order data
     * @param response Reservation/release response (can be null)
     */
    private void publishInventoryEvent(InventoryEventType eventType,
                                       OrderEvent.OrderData order,
                                       ReservationResponse response) {
        try {
            InventoryEvent inventoryEvent = InventoryEvent.builder()
                    .eventType(eventType.name())
                    .eventId(UUID.randomUUID().toString())
                    .timestamp(LocalDateTime.now())
                    .orderId(order.getOrderId())
                    .orderNumber(order.getOrderNumber())
                    .warehouseId(order.getWarehouseId())
                    .success(response != null && response.getSuccess())
                    .message(response != null ? response.getMessage() : "Inventory event")
                    .build();

            kafkaTemplate.send("inventory.events", inventoryEvent);
            log.info("Published {} event for order {}", eventType, order.getOrderNumber());

        } catch (Exception e) {
            log.error("Error publishing inventory event", e);
        }
    }
}

/**
 * Inventory Event DTO for Kafka messages
 */
@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
class InventoryEvent {
    private String eventType;
    private String eventId;
    private LocalDateTime timestamp;
    private Long orderId;
    private String orderNumber;
    private Long warehouseId;
    private Boolean success;
    private String message;
}
