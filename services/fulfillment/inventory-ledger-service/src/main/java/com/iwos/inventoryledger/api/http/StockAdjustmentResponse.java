package com.iwos.inventoryledger.api.http;

import java.time.Instant;
import java.util.UUID;

public record StockAdjustmentResponse(
        UUID adjustmentId,
        String nodeId,
        String sku,
        int quantityDelta,
        String reason,
        String referenceType,
        String referenceId,
        InventoryStockResponse stock,
        Instant createdAt
) {
}
