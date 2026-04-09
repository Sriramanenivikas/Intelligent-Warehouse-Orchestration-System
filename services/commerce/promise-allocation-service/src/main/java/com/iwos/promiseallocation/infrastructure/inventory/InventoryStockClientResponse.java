package com.iwos.promiseallocation.infrastructure.inventory;

import java.time.Instant;

public record InventoryStockClientResponse(
        String nodeId,
        String sku,
        int onHandQuantity,
        int reservedQuantity,
        int availableQuantity,
        Instant updatedAt
) {
}
