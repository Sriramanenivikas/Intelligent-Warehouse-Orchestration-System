package com.iwos.orderorchestrator.infrastructure.inventory;

public record InventoryReservationActionRequest(
        String reason,
        String referenceType,
        String referenceId
) {
}
