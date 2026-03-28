package com.iwos.orderintake.domain.order;

import java.util.UUID;

public class OrderIntentNotFoundException extends RuntimeException {

    public OrderIntentNotFoundException(UUID orderIntentId) {
        super("Order intent %s was not found".formatted(orderIntentId));
    }
}
