package com.iwos.shipmenthandoff.infrastructure.taskexecution;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderIntentResponse(
        UUID orderIntentId,
        String customerId,
        String channel,
        String paymentMode,
        String currency,
        java.math.BigDecimal totalAmount,
        String status,
        Instant acceptedAt,
        DeliveryAddress deliveryAddress,
        List<OrderItem> items
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DeliveryAddress(
            String name,
            String line1,
            String line2,
            String city,
            String state,
            String postalCode,
            String country,
            String phone
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OrderItem(
            String sku,
            int quantity
    ) {}
}
