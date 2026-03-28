package com.iwos.orderorchestrator.domain.workflow;

import java.util.UUID;

public class OrderIntentSourceEventNotFoundException extends RuntimeException {

    public OrderIntentSourceEventNotFoundException(UUID orderIntentId) {
        super("Accepted order intent event not found for orderIntentId=%s".formatted(orderIntentId));
    }
}
