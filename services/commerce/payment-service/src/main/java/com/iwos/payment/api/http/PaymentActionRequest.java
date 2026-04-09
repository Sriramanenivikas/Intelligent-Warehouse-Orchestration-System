package com.iwos.payment.api.http;

public record PaymentActionRequest(
        String reason,
        String referenceType,
        String referenceId
) {
}
