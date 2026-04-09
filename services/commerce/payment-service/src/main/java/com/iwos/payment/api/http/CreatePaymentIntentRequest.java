package com.iwos.payment.api.http;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record CreatePaymentIntentRequest(
        @NotNull UUID orderIntentId,
        @NotNull UUID orderWorkflowId,
        @NotBlank String customerId,
        @NotBlank String paymentMode,
        @NotBlank String currency,
        @NotNull @DecimalMin(value = "0.01") BigDecimal totalAmount
) {
}
