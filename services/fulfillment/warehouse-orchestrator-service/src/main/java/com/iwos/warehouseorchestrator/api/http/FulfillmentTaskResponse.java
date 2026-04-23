package com.iwos.warehouseorchestrator.api.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.iwos.warehouseorchestrator.domain.fulfillment.FulfillmentTaskStatus;
import com.iwos.warehouseorchestrator.domain.fulfillment.FulfillmentTaskType;
import java.time.Instant;
import java.util.UUID;

public record FulfillmentTaskResponse(
        UUID fulfillmentTaskId,
        FulfillmentTaskType taskType,
        FulfillmentTaskStatus taskStatus,
        int sequenceNumber,
        String nodeId,
        String taskTitle,
        JsonNode taskPayload,
        Instant createdAt,
        Instant updatedAt
) {
}
