package com.iwos.shipmentnetwork.api.http;

import java.time.Instant;
import java.util.UUID;

public record ScanEventResponse(
        UUID scanEventId,
        UUID shipmentId,
        String awbNumber,
        String scanType,
        String nodeId,
        String facilityCode,
        String notes,
        Instant occurredAt
) {
}
