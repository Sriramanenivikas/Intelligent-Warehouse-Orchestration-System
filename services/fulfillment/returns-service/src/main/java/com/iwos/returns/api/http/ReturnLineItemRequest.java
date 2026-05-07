package com.iwos.returns.api.http;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ReturnLineItemRequest(
        @NotBlank String sku,
        @Min(1) int quantity
) {
}
