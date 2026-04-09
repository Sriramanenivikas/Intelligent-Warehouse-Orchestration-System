package com.iwos.orderorchestrator.infrastructure.promise;

public record PromiseItemRequest(
        String sku,
        int quantity
) {
}
