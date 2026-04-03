package com.iwos.orderorchestrator.infrastructure.promise;

import java.util.List;

public record ResolvePromiseRequest(
        String customerId,
        PromiseAddressRequest deliveryAddress,
        List<PromiseItemRequest> items
) {
}
