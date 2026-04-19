package com.iwos.shipmentnetwork.domain.network;

import java.util.UUID;

public class NetworkShipmentStateException extends RuntimeException {

    public NetworkShipmentStateException(UUID shipmentId, NetworkShipmentStatus status, ScanType scanType) {
        super("Cannot apply scan " + scanType + " to shipment " + shipmentId + " in status " + status);
    }
}
