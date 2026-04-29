package com.iwos.warehouseorchestrator.infrastructure.orderworkflow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderWorkflowPaymentClientResponse(
        UUID paymentIntentId,
        String status,
        String providerReference,
        String failureReason,
        Instant processedAt
) {
}
