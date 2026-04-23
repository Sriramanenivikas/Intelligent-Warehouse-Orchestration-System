package com.iwos.warehouseorchestrator.infrastructure.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import com.iwos.warehouseorchestrator.api.http.FulfillmentOrderResponse;
import com.iwos.warehouseorchestrator.api.http.FulfillmentTaskResponse;
import com.iwos.warehouseorchestrator.infrastructure.persistence.entity.FulfillmentOrderEntity;
import com.iwos.warehouseorchestrator.infrastructure.persistence.entity.FulfillmentTaskEntity;
import java.util.Comparator;
import org.springframework.stereotype.Component;

@Component
public class FulfillmentOrderResponseMapper {

    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    public FulfillmentOrderResponseMapper(com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public FulfillmentOrderResponse toResponse(FulfillmentOrderEntity fulfillmentOrder) {
        return new FulfillmentOrderResponse(
                fulfillmentOrder.getFulfillmentOrderId(),
                fulfillmentOrder.getWorkflowId(),
                fulfillmentOrder.getOrderIntentId(),
                fulfillmentOrder.getPaymentIntentId(),
                fulfillmentOrder.getCustomerId(),
                fulfillmentOrder.getFulfillmentNodeId(),
                fulfillmentOrder.getWarehouseCode(),
                fulfillmentOrder.getSourceTopic(),
                fulfillmentOrder.getSourceMessageKey(),
                fulfillmentOrder.getSourceEventType(),
                fulfillmentOrder.getStatus(),
                fulfillmentOrder.getSourceEventOccurredAt(),
                fulfillmentOrder.getCreatedAt(),
                fulfillmentOrder.getUpdatedAt(),
                readJson(fulfillmentOrder.getSourceEventPayload()),
                fulfillmentOrder.getTasks().stream()
                        .sorted(Comparator.comparingInt(FulfillmentTaskEntity::getSequenceNumber))
                        .map(this::toTaskResponse)
                        .toList()
        );
    }

    private FulfillmentTaskResponse toTaskResponse(FulfillmentTaskEntity task) {
        return new FulfillmentTaskResponse(
                task.getFulfillmentTaskId(),
                task.getTaskType(),
                task.getStatus(),
                task.getSequenceNumber(),
                task.getNodeId(),
                task.getTaskTitle(),
                readJson(task.getTaskPayload()),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }

    private JsonNode readJson(String value) {
        try {
            return value == null || value.isBlank() ? objectMapper.nullNode() : objectMapper.readTree(value);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to parse stored payload", exception);
        }
    }
}
