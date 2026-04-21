package com.iwos.shipmentnetwork.api.http;

import com.iwos.shipmentnetwork.domain.network.ScanType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record ScanRequest(
        @NotNull ScanType scanType,
        @Size(max = 64) String nodeId,
        @Size(max = 64) String facilityCode,
        @Size(max = 512) String notes,
        Instant occurredAt
) {
}
