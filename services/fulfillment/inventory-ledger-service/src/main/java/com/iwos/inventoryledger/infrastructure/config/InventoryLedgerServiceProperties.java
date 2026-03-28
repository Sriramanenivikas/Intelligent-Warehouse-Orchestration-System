package com.iwos.inventoryledger.infrastructure.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "iwos.inventory-ledger")
public class InventoryLedgerServiceProperties {

    private Duration idempotencyCacheTtl = Duration.ofHours(24);
    private Duration reservationTtl = Duration.ofMinutes(15);

    public Duration getIdempotencyCacheTtl() {
        return idempotencyCacheTtl;
    }

    public void setIdempotencyCacheTtl(Duration idempotencyCacheTtl) {
        this.idempotencyCacheTtl = idempotencyCacheTtl;
    }

    public Duration getReservationTtl() {
        return reservationTtl;
    }

    public void setReservationTtl(Duration reservationTtl) {
        this.reservationTtl = reservationTtl;
    }
}
