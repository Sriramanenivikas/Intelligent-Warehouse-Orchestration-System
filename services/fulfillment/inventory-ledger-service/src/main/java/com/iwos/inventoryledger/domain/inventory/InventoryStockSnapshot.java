package com.iwos.inventoryledger.domain.inventory;

import java.time.Instant;

public record InventoryStockSnapshot(
        String nodeId,
        String sku,
        int onHandQuantity,
        int reservedQuantity,
        int availableQuantity,
        Instant updatedAt
) {
}
