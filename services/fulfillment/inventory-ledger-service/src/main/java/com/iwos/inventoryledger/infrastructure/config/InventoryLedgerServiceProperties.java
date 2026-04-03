package com.iwos.inventoryledger.infrastructure.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "iwos.inventory-ledger")
public class InventoryLedgerServiceProperties {

    private Duration idempotencyCacheTtl = Duration.ofHours(24);
    private Duration reservationTtl = Duration.ofMinutes(15);
    private OutboxProperties outbox = new OutboxProperties();

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

    public OutboxProperties getOutbox() {
        return outbox;
    }

    public void setOutbox(OutboxProperties outbox) {
        this.outbox = outbox;
    }

    public static class OutboxProperties {
        private String topic = "iwos.inventory.events.v1";
        private Duration pollInterval = Duration.ofSeconds(5);
        private int batchSize = 50;

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public Duration getPollInterval() {
            return pollInterval;
        }

        public void setPollInterval(Duration pollInterval) {
            this.pollInterval = pollInterval;
        }

        public int getBatchSize() {
            return batchSize;
        }

        public void setBatchSize(int batchSize) {
            this.batchSize = batchSize;
        }
    }
}
