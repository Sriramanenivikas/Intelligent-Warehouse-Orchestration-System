package com.iwos.orderorchestrator.infrastructure.payment;

import java.time.Instant;
import java.util.Map;

public record PaymentServiceErrorResponse(
        String code,
        String message,
        String requestId,
        Instant timestamp,
        Map<String, String> fieldErrors
) {
}
