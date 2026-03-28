package com.iwos.orderorchestrator.api.http;

import com.iwos.orderorchestrator.domain.workflow.OrderWorkflowReservationStatus;
import java.time.Instant;
import java.util.UUID;

public record OrderWorkflowReservationResponse(
        UUID workflowReservationId,
        UUID orderIntentItemId,
        UUID inventoryReservationId,
        String nodeId,
        String sku,
        int quantity,
        OrderWorkflowReservationStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
