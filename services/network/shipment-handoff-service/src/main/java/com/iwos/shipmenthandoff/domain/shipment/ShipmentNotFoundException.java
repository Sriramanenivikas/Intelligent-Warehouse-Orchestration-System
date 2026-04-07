package com.iwos.shipmenthandoff.domain.shipment;

import java.util.UUID;

public class ShipmentNotFoundException extends RuntimeException {

    private final UUID identifier;

    public ShipmentNotFoundException(UUID identifier) {
        super("Shipment not found: " + identifier);
        this.identifier = identifier;
    }

    public UUID getIdentifier() {
        return identifier;
    }
}
