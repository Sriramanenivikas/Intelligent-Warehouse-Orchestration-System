package com.iwos.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Order Domain Model (for CQRS Write Side)
 *
 * This is a simplified domain model used in the command handler.
 * It represents the core business concept of an Order.
 *
 * Different from the JPA entity - this is pure domain logic without persistence annotations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    private String id;
    private String orderNumber;
    private String customerId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String warehouseId;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String deliveryType;
    private String paymentMethod;
    private Instant createdAt;
    private Instant updatedAt;

    /**
     * Create a new order
     */
    public static Order create(
            String id,
            String orderNumber,
            String customerId,
            String warehouseId,
            BigDecimal totalAmount,
            String deliveryType,
            String paymentMethod) {

        return Order.builder()
                .id(id)
                .orderNumber(orderNumber)
                .customerId(customerId)
                .warehouseId(warehouseId)
                .status(OrderStatus.PENDING)
                .totalAmount(totalAmount)
                .deliveryType(deliveryType)
                .paymentMethod(paymentMethod)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    /**
     * Confirm order
     */
    public void confirm() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("Can only confirm pending orders");
        }
        this.status = OrderStatus.CONFIRMED;
        this.updatedAt = Instant.now();
    }

    /**
     * Cancel order
     */
    public void cancel() {
        if (this.status == OrderStatus.DELIVERED || this.status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot cancel order in status: " + this.status);
        }
        this.status = OrderStatus.CANCELLED;
        this.updatedAt = Instant.now();
    }

    /**
     * Update status
     */
    public void updateStatus(OrderStatus newStatus) {
        this.status = newStatus;
        this.updatedAt = Instant.now();
    }
}
