package com.iwos.promiseallocation.api.http;

import jakarta.validation.constraints.NotBlank;

public record AddressPayload(
        @NotBlank String name,
        @NotBlank String line1,
        String line2,
        @NotBlank String city,
        @NotBlank String state,
        @NotBlank String postalCode,
        @NotBlank String country,
        @NotBlank String phone
) {
}
