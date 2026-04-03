package com.iwos.orderorchestrator.infrastructure.promise;

public record PromiseItemDecisionResponse(
        String sku,
        int requestedQuantity,
        int availableQuantity,
        boolean fulfillable
) {
}
