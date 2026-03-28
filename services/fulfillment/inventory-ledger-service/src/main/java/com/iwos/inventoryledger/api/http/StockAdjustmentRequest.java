package com.iwos.inventoryledger.api.http;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StockAdjustmentRequest(
        @NotBlank String nodeId,
        @NotBlank String sku,
        @NotNull Integer quantityDelta,
        @NotBlank String reason,
        String referenceType,
        String referenceId
) {

    @AssertTrue(message = "quantityDelta must not be zero")
    public boolean isQuantityDeltaNonZero() {
        return quantityDelta != null && quantityDelta != 0;
    }
}
