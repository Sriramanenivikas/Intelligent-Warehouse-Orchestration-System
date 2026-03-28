package com.iwos.orderorchestrator.infrastructure.inventory;

import java.time.Instant;
import java.util.Map;

public record InventoryServiceErrorResponse(
        String code,
        String message,
        String requestId,
        Instant timestamp,
        Map<String, String> fieldErrors
) {
}
