package com.iwos.returns.infrastructure.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwos.returns.api.http.ReturnLineItemRequest;
import com.iwos.returns.api.http.ReturnLineItemResponse;
import com.iwos.returns.api.http.ReturnResponse;
import com.iwos.returns.infrastructure.persistence.entity.ReturnRequestEntity;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ReturnResponseMapper {

    private static final TypeReference<List<ReturnLineItemRequest>> LINE_ITEM_LIST = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;

    public ReturnResponseMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ReturnResponse toResponse(ReturnRequestEntity entity) {
        List<ReturnLineItemResponse> items = readItems(entity.getItemsJson()).stream()
                .map(item -> new ReturnLineItemResponse(item.sku(), item.quantity()))
                .toList();
        return new ReturnResponse(
                entity.getReturnRequestId(),
                entity.getOrderIntentId(),
                entity.getFulfillmentOrderId(),
                entity.getShipmentId(),
                entity.getCustomerId(),
                entity.getNodeId(),
                entity.getReasonCode(),
                entity.getReasonDetail(),
                entity.getStatus(),
                entity.getItemCount(),
                items,
                entity.getRequestedAt(),
                entity.getApprovedAt(),
                entity.getReceivedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public String writeItems(List<ReturnLineItemRequest> items) {
        try {
            return objectMapper.writeValueAsString(items);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to serialize return items", exception);
        }
    }

    private List<ReturnLineItemRequest> readItems(String itemsJson) {
        try {
            return objectMapper.readValue(itemsJson, LINE_ITEM_LIST);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to deserialize return items", exception);
        }
    }
}
