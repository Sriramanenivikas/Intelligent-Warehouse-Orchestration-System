package com.iwos.inventoryledger.api.http;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateInventoryReservationRequest(
        @NotBlank String orderReference,
        @NotBlank String nodeId,
        @NotBlank String sku,
        @Min(1) int quantity
) {
}
