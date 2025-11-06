package com.iwos.cqrs.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * CQRS COMMAND - Write Operation
 *
 * Commands represent intent to change state
 * They are validated and then executed
 * Result in domain events being published
 *
 * Command vs Event:
 * - Command: "Please create order" (can fail)
 * - Event: "Order was created" (already happened, cannot fail)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderCommand {

    private String commandId;  // For idempotency
    private String customerId;
    private List<OrderItemDTO> items;
    private DeliveryAddressDTO deliveryAddress;
    private String deliveryType;  // EXPRESS, STANDARD
    private String paymentMethod;
    private BigDecimal totalAmount;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDTO {
        private String sku;
        private Integer quantity;
        private BigDecimal unitPrice;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveryAddressDTO {
        private String line1;
        private String line2;
        private String city;
        private String state;
        private String pincode;
        private Double latitude;   // For geospatial routing
        private Double longitude;
    }

    /**
     * Validate command before execution
     */
    public void validate() {
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("Customer ID is required");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one item");
        }
        if (deliveryAddress == null) {
            throw new IllegalArgumentException("Delivery address is required");
        }
        if (deliveryAddress.getLatitude() == null || deliveryAddress.getLongitude() == null) {
            throw new IllegalArgumentException("Geolocation is required for delivery address");
        }
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Total amount must be positive");
        }
    }
}
