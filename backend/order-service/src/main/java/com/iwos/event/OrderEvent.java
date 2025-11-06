package com.iwos.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

/**
 * Order Domain Event
 *
 * Event Types:
 * - order.created
 * - order.confirmed
 * - order.cancelled
 * - order.shipped
 *
 * Architecture Pattern: Event-Driven Choreography
 * When published, other services react independently
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {

    // Event metadata
    private String eventId;
    private String eventType;
    private Instant timestamp;
    private String source;
    private String correlationId;

    // Order data
    private Long orderId;
    private String orderNumber;
    private String status;
    private Long warehouseId;
    private BigDecimal totalAmount;
    private Integer totalItems;

    // Additional context
    private Map<String, Object> metadata;

    /**
     * Create Order Created Event
     */
    public static OrderEvent orderCreated(Long orderId, String orderNumber, Long warehouseId,
                                          BigDecimal totalAmount, Integer totalItems) {
        return OrderEvent.builder()
            .eventId(java.util.UUID.randomUUID().toString())
            .eventType("order.created")
            .timestamp(Instant.now())
            .source("order-service")
            .orderId(orderId)
            .orderNumber(orderNumber)
            .status("PENDING")
            .warehouseId(warehouseId)
            .totalAmount(totalAmount)
            .totalItems(totalItems)
            .build();
    }

    /**
     * Create Order Cancelled Event
     */
    public static OrderEvent orderCancelled(Long orderId, String orderNumber, String reason) {
        return OrderEvent.builder()
            .eventId(java.util.UUID.randomUUID().toString())
            .eventType("order.cancelled")
            .timestamp(Instant.now())
            .source("order-service")
            .orderId(orderId)
            .orderNumber(orderNumber)
            .status("CANCELLED")
            .metadata(Map.of("reason", reason))
            .build();
    }

    /**
     * Create Order Status Changed Event
     */
    public static OrderEvent orderStatusChanged(Long orderId, String orderNumber, String oldStatus, String newStatus) {
        return OrderEvent.builder()
            .eventId(java.util.UUID.randomUUID().toString())
            .eventType("order.status.changed")
            .timestamp(Instant.now())
            .source("order-service")
            .orderId(orderId)
            .orderNumber(orderNumber)
            .status(newStatus)
            .metadata(Map.of("oldStatus", oldStatus, "newStatus", newStatus))
            .build();
    }
}
