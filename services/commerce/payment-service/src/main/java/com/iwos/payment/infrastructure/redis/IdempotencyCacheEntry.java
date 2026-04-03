package com.iwos.payment.infrastructure.redis;

import com.iwos.payment.api.http.PaymentIntentResponse;

public record IdempotencyCacheEntry(String requestHash, PaymentIntentResponse response) {
}
