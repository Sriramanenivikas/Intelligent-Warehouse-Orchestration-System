package com.iwos.payment.api.http;

import com.iwos.payment.domain.payment.PaymentIntentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentIntentResponse(
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
        PaymentIntentStatus status,
        String failureReason,
        Instant authorizedAt,
        Instant succeededAt,
        Instant failedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
