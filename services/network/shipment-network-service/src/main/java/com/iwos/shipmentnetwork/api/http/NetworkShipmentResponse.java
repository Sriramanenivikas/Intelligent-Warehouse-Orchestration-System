package com.iwos.shipmentnetwork.api.http;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record NetworkShipmentResponse(
        UUID networkShipmentId,
        UUID shipmentId,
        UUID fulfillmentOrderId,
        UUID orderIntentId,
        String awbNumber,
        String carrierCode,
        String customerId,
        String originNodeId,
        String currentNodeId,
        String currentFacilityCode,
        String status,
        String lastScanType,
        Instant lastScannedAt,
        Instant createdAt,
        Instant updatedAt,
        List<ScanEventResponse> scans
) {
}
