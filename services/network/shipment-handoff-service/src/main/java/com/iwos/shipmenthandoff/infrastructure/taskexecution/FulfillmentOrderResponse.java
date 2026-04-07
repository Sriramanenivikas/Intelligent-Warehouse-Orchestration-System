package com.iwos.shipmenthandoff.infrastructure.taskexecution;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FulfillmentOrderResponse(
        UUID fulfillmentOrderId,
        UUID workflowId,
        UUID orderIntentId,
        UUID paymentIntentId,
        String customerId,
        String fulfillmentNodeId,
        String warehouseCode,
        String status,
        Instant createdAt,
        Instant updatedAt,
        List<TaskItem> tasks
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TaskItem(
            UUID fulfillmentTaskId,
            String taskType,
            String taskStatus,
            int sequenceNumber,
            String nodeId,
            String taskTitle,
            Map<String, Object> taskPayload,
            Instant createdAt,
            Instant updatedAt
    ) {}
}
