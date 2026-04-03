package com.iwos.promiseallocation.api.http;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record PromiseItemRequest(
        @NotBlank String sku,
        @Positive int quantity
) {
}
