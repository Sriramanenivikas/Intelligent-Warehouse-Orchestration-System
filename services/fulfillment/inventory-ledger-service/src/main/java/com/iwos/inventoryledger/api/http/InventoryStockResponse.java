package com.iwos.inventoryledger.api.http;

import java.time.Instant;

public record InventoryStockResponse(
        String nodeId,
        String sku,
        int onHandQuantity,
        int reservedQuantity,
        int availableQuantity,
        Instant updatedAt
) {
}
