package com.iwos.returns.api.http;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ReturnResponse(
        UUID returnRequestId,
        UUID orderIntentId,
        UUID fulfillmentOrderId,
        UUID shipmentId,
        String customerId,
        String nodeId,
        String reasonCode,
        String reasonDetail,
        String status,
        int itemCount,
        List<ReturnLineItemResponse> items,
        Instant requestedAt,
        Instant approvedAt,
        Instant receivedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
