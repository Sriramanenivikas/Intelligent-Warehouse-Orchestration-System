package com.iwos.orderintake.infrastructure.redis;

import com.iwos.orderintake.api.http.OrderIntentResponse;

public record IdempotencyCacheEntry(
        String requestHash,
        OrderIntentResponse response
) {
}
