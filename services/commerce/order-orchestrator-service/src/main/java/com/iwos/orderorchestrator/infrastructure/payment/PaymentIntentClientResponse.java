package com.iwos.orderorchestrator.infrastructure.payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentIntentClientResponse(
        UUID paymentIntentId,
        UUID orderIntentId,
        UUID orderWorkflowId,
        String customerId,
        String paymentMode,
        String currency,
        BigDecimal totalAmount,
        BigDecimal capturedAmount,
        String providerName,
        String providerReference,
        String status,
        String failureReason,
        Instant authorizedAt,
        Instant succeededAt,
        Instant failedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
