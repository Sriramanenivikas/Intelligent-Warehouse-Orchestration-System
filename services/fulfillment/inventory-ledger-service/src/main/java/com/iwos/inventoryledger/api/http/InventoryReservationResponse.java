package com.iwos.inventoryledger.api.http;

import com.iwos.inventoryledger.domain.reservation.InventoryReservationStatus;
import java.time.Instant;
import java.util.UUID;

public record InventoryReservationResponse(
        UUID reservationId,
        String orderReference,
        String nodeId,
        String sku,
        int quantity,
        InventoryReservationStatus status,
        Instant expiresAt,
        Instant createdAt,
        InventoryStockResponse stock
) {
}
