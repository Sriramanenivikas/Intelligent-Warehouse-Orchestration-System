package com.iwos.inventoryledger.domain.reservation;

import java.util.UUID;

public class ReservationStateConflictException extends RuntimeException {

    public ReservationStateConflictException(UUID reservationId, String message) {
        super("Reservation %s %s".formatted(reservationId, message));
    }
}
