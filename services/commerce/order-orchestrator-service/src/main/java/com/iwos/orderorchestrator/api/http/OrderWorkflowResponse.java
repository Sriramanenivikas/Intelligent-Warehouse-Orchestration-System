package com.iwos.orderorchestrator.api.http;

import com.iwos.orderorchestrator.domain.workflow.OrderWorkflowStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderWorkflowResponse(
        UUID workflowId,
        UUID orderIntentId,
        UUID sourceOutboxEventId,
        String customerId,
        String fulfillmentNodeId,
        OrderWorkflowStatus status,
        String failureReason,
        Instant acceptedAt,
        Instant completedAt,
        Instant createdAt,
        Instant updatedAt,
        OrderWorkflowPaymentResponse payment,
        List<OrderWorkflowReservationResponse> reservations
) {
}
