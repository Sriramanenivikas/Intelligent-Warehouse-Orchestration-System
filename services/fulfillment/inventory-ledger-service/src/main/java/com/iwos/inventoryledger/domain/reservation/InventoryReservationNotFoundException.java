package com.iwos.inventoryledger.domain.reservation;

import java.util.UUID;

public class InventoryReservationNotFoundException extends RuntimeException {

    public InventoryReservationNotFoundException(UUID reservationId) {
        super("Inventory reservation not found for reservationId=%s".formatted(reservationId));
    }
}
