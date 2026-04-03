package com.iwos.orderorchestrator.infrastructure.promise;

public record PromiseAddressRequest(
        String name,
        String line1,
        String line2,
        String city,
        String state,
        String postalCode,
        String country,
        String phone
) {
}
