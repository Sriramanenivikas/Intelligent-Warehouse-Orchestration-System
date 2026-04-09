package com.iwos.shipmenthandoff.infrastructure.persistence;

import com.iwos.shipmenthandoff.api.http.ShipmentResponse;
import com.iwos.shipmenthandoff.infrastructure.persistence.entity.ShipmentEntity;
import org.springframework.stereotype.Component;

@Component
public class ShipmentResponseMapper {

    public ShipmentResponse toResponse(ShipmentEntity entity) {
        ShipmentResponse.DestinationAddress address = new ShipmentResponse.DestinationAddress(
                entity.getDestinationName(),
                entity.getDestinationPhone(),
                entity.getDestinationLine1(),
                entity.getDestinationLine2(),
                entity.getDestinationCity(),
                entity.getDestinationState(),
                entity.getDestinationPostalCode(),
                entity.getDestinationCountry()
        );

        return new ShipmentResponse(
                entity.getShipmentId(),
                entity.getFulfillmentOrderId(),
                entity.getOrderIntentId(),
                entity.getWorkflowId(),
                entity.getCustomerId(),
                entity.getAwbNumber(),
                entity.getCarrierCode(),
                entity.getStatus(),
                entity.getOriginNodeId(),
                address,
                entity.getWeightGrams(),
                entity.getPackageCount(),
                entity.getEstimatedDeliveryAt(),
                entity.getManifestedAt(),
                entity.getDispatchedAt(),
                entity.getDeliveredAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
