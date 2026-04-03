package com.iwos.orderintake.infrastructure.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwos.orderintake.api.http.AddressPayload;
import com.iwos.orderintake.api.http.OrderIntentResponse;
import com.iwos.orderintake.api.http.OrderItemPayload;
import com.iwos.orderintake.infrastructure.persistence.entity.OrderIntentEntity;
import java.util.Comparator;
import org.springframework.stereotype.Component;

@Component
public class OrderIntentResponseMapper {

    private final ObjectMapper objectMapper;

    public OrderIntentResponseMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public OrderIntentResponse toResponse(OrderIntentEntity entity) {
        try {
            return new OrderIntentResponse(
                    entity.getOrderIntentId(),
                    entity.getCustomerId(),
                    entity.getChannel(),
                    entity.getPaymentMode(),
                    entity.getCurrency(),
                    entity.getTotalAmount(),
                    entity.getStatus(),
                    entity.getAcceptedAt(),
                    objectMapper.readValue(entity.getDeliveryAddressJson(), AddressPayload.class),
                    entity.getItems().stream()
                            .sorted(Comparator.comparing(item -> item.getOrderIntentItemId().toString()))
                            .map(item -> new OrderItemPayload(item.getSku(), item.getQuantity()))
                            .toList()
            );
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to map order intent response", exception);
        }
    }
}
