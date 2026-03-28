package com.iwos.orderorchestrator.infrastructure.inventory;

public record InventoryCreateReservationRequest(
        String orderReference,
        String nodeId,
        String sku,
        int quantity
) {
}
