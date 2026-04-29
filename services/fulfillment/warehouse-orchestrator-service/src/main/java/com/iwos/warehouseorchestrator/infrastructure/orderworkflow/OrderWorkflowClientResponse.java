package com.iwos.warehouseorchestrator.infrastructure.orderworkflow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderWorkflowClientResponse(
        UUID workflowId,
        UUID orderIntentId,
        UUID sourceOutboxEventId,
        String customerId,
        String fulfillmentNodeId,
        String status,
        String failureReason,
        Instant acceptedAt,
        Instant completedAt,
        Instant createdAt,
        Instant updatedAt,
        OrderWorkflowPaymentClientResponse payment,
        List<OrderWorkflowReservationClientResponse> reservations
) {
}
