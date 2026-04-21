package com.iwos.scanevent.api.http;

import java.time.Instant;
import java.util.UUID;

public record NormalizedScanEventResponse(
        UUID normalizedScanEventId,
        UUID scanEventId,
        String sourceEventType,
        String scanType,
        String statusAfterEvent,
        String nodeId,
        String facilityCode,
        String notes,
        Instant occurredAt,
        Instant ingestedAt
) {
}
