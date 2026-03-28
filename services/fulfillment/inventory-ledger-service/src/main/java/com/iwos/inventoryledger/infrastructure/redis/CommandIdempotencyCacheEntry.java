package com.iwos.inventoryledger.infrastructure.redis;

public record CommandIdempotencyCacheEntry(
        String operationType,
        String requestHash,
        String responseBody
) {
}
