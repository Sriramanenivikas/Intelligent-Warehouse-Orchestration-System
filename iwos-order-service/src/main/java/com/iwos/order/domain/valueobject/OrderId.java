package com.iwos.order.domain.valueobject;

public record OrderId(String value) {
    public OrderId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("OrderId cannot be null or blank");
        }
    }
}
