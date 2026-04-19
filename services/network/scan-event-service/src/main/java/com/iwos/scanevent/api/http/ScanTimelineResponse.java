package com.iwos.scanevent.api.http;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ScanTimelineResponse(
        UUID trackedShipmentId,
        UUID shipmentId,
        UUID networkShipmentId,
        UUID fulfillmentOrderId,
        UUID orderIntentId,
        String awbNumber,
        String carrierCode,
        String customerId,
        String currentStatus,
        String lastScanType,
        Instant lastScannedAt,
        Instant createdAt,
        Instant updatedAt,
        List<NormalizedScanEventResponse> events
) {
}
