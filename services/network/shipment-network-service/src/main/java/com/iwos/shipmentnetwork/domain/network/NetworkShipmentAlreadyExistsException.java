package com.iwos.shipmentnetwork.domain.network;

import java.util.UUID;

public class NetworkShipmentAlreadyExistsException extends RuntimeException {

    public NetworkShipmentAlreadyExistsException(UUID shipmentId) {
        super("Network shipment already exists: " + shipmentId);
    }
}
