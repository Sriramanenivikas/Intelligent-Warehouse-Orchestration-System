package com.iwos.returns.api.http;

public record ReturnLineItemResponse(
        String sku,
        int quantity
) {
}
