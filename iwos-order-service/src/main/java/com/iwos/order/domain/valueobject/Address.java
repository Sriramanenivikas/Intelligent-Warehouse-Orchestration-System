package com.iwos.order.domain.valueobject;

public record Address(
    String line1,
    String line2,
    String city,
    String state,
    String pincode,
    String country,
    Double latitude,
    Double longitude
) {}
