package com.iwos.orderorchestrator.infrastructure.persistence;

import com.iwos.orderorchestrator.api.http.OrderWorkflowReservationResponse;
import com.iwos.orderorchestrator.api.http.OrderWorkflowResponse;
import com.iwos.orderorchestrator.infrastructure.persistence.entity.OrderWorkflowEntity;
import java.util.Comparator;
import org.springframework.stereotype.Component;

@Component
public class OrderWorkflowResponseMapper {

    public OrderWorkflowResponse toResponse(OrderWorkflowEntity workflow) {
        return new OrderWorkflowResponse(
                workflow.getWorkflowId(),
                workflow.getOrderIntentId(),
                workflow.getSourceOutboxEventId(),
                workflow.getCustomerId(),
                workflow.getFulfillmentNodeId(),
                workflow.getStatus(),
                workflow.getFailureReason(),
                workflow.getAcceptedAt(),
                workflow.getCompletedAt(),
                workflow.getCreatedAt(),
                workflow.getUpdatedAt(),
                workflow.getReservations().stream()
                        .sorted(Comparator.comparing(reservation -> reservation.getCreatedAt() == null
                                ? workflow.getCreatedAt()
                                : reservation.getCreatedAt()))
                        .map(reservation -> new OrderWorkflowReservationResponse(
                                reservation.getWorkflowReservationId(),
                                reservation.getOrderIntentItemId(),
                                reservation.getInventoryReservationId(),
                                reservation.getNodeId(),
                                reservation.getSku(),
                                reservation.getQuantity(),
                                reservation.getStatus(),
                                reservation.getCreatedAt(),
                                reservation.getUpdatedAt()
                        ))
                        .toList()
        );
    }
}
