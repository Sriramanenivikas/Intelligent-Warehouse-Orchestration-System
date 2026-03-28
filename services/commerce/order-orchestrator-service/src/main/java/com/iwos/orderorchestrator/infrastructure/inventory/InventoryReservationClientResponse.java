package com.iwos.orderorchestrator.infrastructure.inventory;

import java.time.Instant;
import java.util.UUID;

public record InventoryReservationClientResponse(
        UUID reservationId,
        String orderReference,
        String nodeId,
        String sku,
        int quantity,
        String status,
        Instant expiresAt,
        Instant createdAt
) {
}
