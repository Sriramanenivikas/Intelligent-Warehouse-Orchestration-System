package com.iwos.warehouseorchestrator.domain.fulfillment;

import java.util.UUID;

public class OrderWorkflowNotReadyException extends RuntimeException {

    public OrderWorkflowNotReadyException(UUID orderIntentId, String actualStatus) {
        super("Order workflow for orderIntentId=%s is not ready for fulfillment (status=%s, expected=PAYMENT_AUTHORIZED)"
                .formatted(orderIntentId, actualStatus));
    }
}
