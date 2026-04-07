package com.iwos.shipmenthandoff.domain.shipment;

import java.util.UUID;

public class ShipmentAlreadyExistsException extends RuntimeException {

    private final UUID fulfillmentOrderId;

    public ShipmentAlreadyExistsException(UUID fulfillmentOrderId) {
        super("Shipment already exists for fulfillment order: " + fulfillmentOrderId);
        this.fulfillmentOrderId = fulfillmentOrderId;
    }

    public UUID getFulfillmentOrderId() {
        return fulfillmentOrderId;
    }
}
