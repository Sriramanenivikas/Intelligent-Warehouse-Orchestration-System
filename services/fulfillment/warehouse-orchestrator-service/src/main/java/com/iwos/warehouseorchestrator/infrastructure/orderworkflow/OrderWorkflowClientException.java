package com.iwos.warehouseorchestrator.infrastructure.orderworkflow;

import java.util.UUID;

public class OrderWorkflowClientException extends RuntimeException {

    private final int httpStatus;
    private final UUID orderIntentId;

    public OrderWorkflowClientException(UUID orderIntentId, int httpStatus, String message) {
        super("Failed to fetch workflow for orderIntentId=%s: HTTP %d — %s"
                .formatted(orderIntentId, httpStatus, message));
        this.httpStatus = httpStatus;
        this.orderIntentId = orderIntentId;
    }

    public OrderWorkflowClientException(UUID orderIntentId, Throwable cause) {
        super("Failed to fetch workflow for orderIntentId=%s: %s".formatted(orderIntentId, cause.getMessage()), cause);
        this.httpStatus = 0;
        this.orderIntentId = orderIntentId;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public UUID getOrderIntentId() {
        return orderIntentId;
    }
}
