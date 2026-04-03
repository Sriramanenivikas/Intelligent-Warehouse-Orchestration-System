package com.iwos.warehouseorchestrator.api.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.iwos.warehouseorchestrator.domain.fulfillment.FulfillmentOrderStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record FulfillmentOrderResponse(
        UUID fulfillmentOrderId,
        UUID workflowId,
        UUID orderIntentId,
        UUID paymentIntentId,
        String customerId,
        String fulfillmentNodeId,
        String warehouseCode,
        String sourceTopic,
        String sourceMessageKey,
        String sourceEventType,
        FulfillmentOrderStatus status,
        Instant sourceEventOccurredAt,
        Instant createdAt,
        Instant updatedAt,
        JsonNode sourceEventPayload,
        List<FulfillmentTaskResponse> tasks
) {
}
