package com.iwos.shipmenthandoff.domain.shipment;

import java.util.UUID;

public class ShipmentStateException extends RuntimeException {

    private final UUID shipmentId;
    private final ShipmentStatus currentStatus;
    private final String attemptedAction;

    public ShipmentStateException(UUID shipmentId, ShipmentStatus currentStatus, String attemptedAction) {
        super(String.format("Cannot %s shipment %s in status %s", attemptedAction, shipmentId, currentStatus));
        this.shipmentId = shipmentId;
        this.currentStatus = currentStatus;
        this.attemptedAction = attemptedAction;
    }

    public UUID getShipmentId() {
        return shipmentId;
    }

    public ShipmentStatus getCurrentStatus() {
        return currentStatus;
    }

    public String getAttemptedAction() {
        return attemptedAction;
    }
}
