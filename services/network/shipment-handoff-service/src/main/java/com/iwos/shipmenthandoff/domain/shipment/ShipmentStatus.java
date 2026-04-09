package com.iwos.shipmenthandoff.domain.shipment;

public enum ShipmentStatus {
    CREATED,
    MANIFESTED,
    DISPATCHED,
    IN_TRANSIT,
    OUT_FOR_DELIVERY,
    DELIVERED,
    FAILED_DELIVERY,
    RETURNED
}
