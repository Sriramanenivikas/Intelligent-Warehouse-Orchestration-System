package com.iwos.inventoryledger.api.http;

public record ReservationActionRequest(
        String reason,
        String referenceType,
        String referenceId
) {
}
