package com.iwos.warehouseorchestrator.infrastructure.orderworkflow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderWorkflowReservationClientResponse(
        UUID workflowReservationId,
        UUID orderIntentItemId,
        UUID inventoryReservationId,
        String nodeId,
        String sku,
        int quantity,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
}
