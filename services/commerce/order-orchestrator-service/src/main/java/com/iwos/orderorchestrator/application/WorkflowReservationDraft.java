package com.iwos.orderorchestrator.application;

import com.iwos.orderorchestrator.domain.workflow.OrderWorkflowReservationStatus;
import java.util.UUID;

public record WorkflowReservationDraft(
        UUID orderIntentItemId,
        UUID inventoryReservationId,
        String nodeId,
        String sku,
        int quantity,
        OrderWorkflowReservationStatus status
) {
    public WorkflowReservationDraft released() {
        return new WorkflowReservationDraft(
                orderIntentItemId,
                inventoryReservationId,
                nodeId,
                sku,
                quantity,
                OrderWorkflowReservationStatus.RELEASED
        );
    }
}
