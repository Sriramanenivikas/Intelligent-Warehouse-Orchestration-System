package com.iwos.scanevent.domain.scan;

import java.util.UUID;

public class TrackedShipmentNotFoundException extends RuntimeException {

    public TrackedShipmentNotFoundException(UUID shipmentId) {
        super("Tracked shipment not found for shipmentId=" + shipmentId);
    }

    public TrackedShipmentNotFoundException(String keyType, String keyValue) {
        super("Tracked shipment not found for " + keyType + "=" + keyValue);
    }
}
