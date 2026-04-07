package com.iwos.shipmenthandoff.api.http;

import com.iwos.shipmenthandoff.domain.shipment.CarrierCode;
import jakarta.validation.constraints.NotNull;

public record CreateShipmentRequest(
        @NotNull(message = "carrierCode is required")
        CarrierCode carrierCode,
        Integer weightGrams,
        Integer packageCount
) {}
