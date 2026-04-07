package com.iwos.shipmenthandoff.api.http;

import com.iwos.shipmenthandoff.domain.shipment.CarrierCode;
import com.iwos.shipmenthandoff.domain.shipment.ShipmentStatus;
import java.time.Instant;
import java.util.UUID;

public record ShipmentResponse(
        UUID shipmentId,
        UUID fulfillmentOrderId,
        UUID orderIntentId,
        UUID workflowId,
        String customerId,
        String awbNumber,
        CarrierCode carrierCode,
        ShipmentStatus status,
        String originNodeId,
        DestinationAddress destinationAddress,
        Integer weightGrams,
        Integer packageCount,
        Instant estimatedDeliveryAt,
        Instant manifestedAt,
        Instant dispatchedAt,
        Instant deliveredAt,
        Instant createdAt,
        Instant updatedAt
) {
    public record DestinationAddress(
            String name,
            String phone,
            String line1,
            String line2,
            String city,
            String state,
            String postalCode,
            String country
    ) {}
}
