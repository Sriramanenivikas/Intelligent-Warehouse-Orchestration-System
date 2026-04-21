package com.iwos.shipmentnetwork.domain.network;

import java.util.UUID;

public class NetworkShipmentNotFoundException extends RuntimeException {

    public NetworkShipmentNotFoundException(UUID shipmentId) {
        super("Network shipment not found: " + shipmentId);
    }
}
