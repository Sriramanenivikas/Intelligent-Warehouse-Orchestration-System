package com.iwos.promiseallocation.api.http;

public record PromiseItemDecisionResponse(
        String sku,
        int requestedQuantity,
        int availableQuantity,
        boolean fulfillable
) {
}
