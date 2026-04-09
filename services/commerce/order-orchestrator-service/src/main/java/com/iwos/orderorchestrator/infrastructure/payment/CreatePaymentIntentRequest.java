package com.iwos.orderorchestrator.infrastructure.payment;

import java.math.BigDecimal;
import java.util.UUID;

public record CreatePaymentIntentRequest(
        UUID orderIntentId,
        UUID orderWorkflowId,
        String customerId,
        String paymentMode,
        String currency,
        BigDecimal totalAmount
) {
}
