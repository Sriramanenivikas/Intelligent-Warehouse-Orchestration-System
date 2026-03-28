package com.iwos.orderintake.api.http;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record OrderItemPayload(
        @NotBlank String sku,
        @Positive int quantity
) {
}
