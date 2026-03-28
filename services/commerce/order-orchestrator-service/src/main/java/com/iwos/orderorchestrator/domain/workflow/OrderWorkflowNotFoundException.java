package com.iwos.orderorchestrator.domain.workflow;

import java.util.UUID;

public class OrderWorkflowNotFoundException extends RuntimeException {

    public OrderWorkflowNotFoundException(UUID orderIntentId) {
        super("Order workflow not found for orderIntentId=%s".formatted(orderIntentId));
    }
}
