package com.iwos.shipmentnetwork.domain.network;

import java.util.UUID;

public class NetworkShipmentNotFoundException extends RuntimeException {

    public NetworkShipmentNotFoundException(UUID shipmentId) {
        super("Network shipment not found: " + shipmentId);
    }

    public NetworkShipmentNotFoundException(String field, String value) {
        super("Network shipment not found for " + field + ": " + value);
    }
}
