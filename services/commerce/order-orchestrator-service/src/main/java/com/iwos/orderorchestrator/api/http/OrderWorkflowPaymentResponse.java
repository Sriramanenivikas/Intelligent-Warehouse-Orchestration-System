package com.iwos.orderorchestrator.api.http;

import com.iwos.orderorchestrator.domain.workflow.OrderWorkflowPaymentStatus;
import java.time.Instant;
import java.util.UUID;

public record OrderWorkflowPaymentResponse(
        UUID paymentIntentId,
        OrderWorkflowPaymentStatus status,
        String providerReference,
        String failureReason,
        Instant processedAt
) {
}
