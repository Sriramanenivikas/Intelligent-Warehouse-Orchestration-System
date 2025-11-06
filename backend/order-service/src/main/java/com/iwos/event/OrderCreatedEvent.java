package com.iwos.event;

import com.iwos.cqrs.command.CreateOrderCommand;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * OrderCreatedEvent - Domain Event
 *
 * Published when a new order is successfully created.
 * This event triggers:
 * 1. Read model update (MongoDB projection)
 * 2. Inventory reservation saga
 * 3. Notification service (email/SMS to customer)
 * 4. Analytics pipeline
 *
 * Event-Driven Architecture: This is a FACT that happened
 * Commands can fail, Events cannot (they already happened)
 *
 * Note: Does not extend DomainEvent to avoid field conflicts.
 * Contains all required fields for Event Store compatibility.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent {

    // Event Metadata (required by Event Store)
    private String eventId;
    private String eventType;
    private String aggregateId;  // orderId
    private String aggregateType;  // "Order"
    private Long version;
    private Instant occurredAt;

    // Order Information
    private String orderId;
    private String orderNumber;
    private String customerId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;

    // Warehouse Information
    private String warehouseId;
    private String warehouseName;
    private Double distanceKm;
    private Integer estimatedDeliveryMinutes;

    // Order Details
    private BigDecimal totalAmount;
    private String deliveryType;
    private String paymentMethod;

    // Items
    private List<CreateOrderCommand.OrderItemDTO> items;

    // Delivery Address
    private CreateOrderCommand.DeliveryAddressDTO deliveryAddress;

    /**
     * Create OrderCreatedEvent from command
     */
    public static OrderCreatedEvent from(
            String orderId,
            String orderNumber,
            CreateOrderCommand command,
            String warehouseId,
            String warehouseName,
            Double distanceKm,
            Integer estimatedDeliveryMinutes) {

        return OrderCreatedEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .eventType("OrderCreated")
                .aggregateId(orderId)
                .aggregateType("Order")
                .version(1L)
                .occurredAt(Instant.now())
                .orderId(orderId)
                .orderNumber(orderNumber)
                .customerId(command.getCustomerId())
                .customerName(command.getCustomerName())
                .customerEmail(command.getCustomerEmail())
                .customerPhone(command.getCustomerPhone())
                .warehouseId(warehouseId)
                .warehouseName(warehouseName)
                .distanceKm(distanceKm)
                .estimatedDeliveryMinutes(estimatedDeliveryMinutes)
                .totalAmount(command.getTotalAmount())
                .deliveryType(command.getDeliveryType())
                .paymentMethod(command.getPaymentMethod())
                .items(command.getItems())
                .deliveryAddress(command.getDeliveryAddress())
                .build();
    }
}
