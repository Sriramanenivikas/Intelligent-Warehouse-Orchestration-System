package com.iwos.orderorchestrator.infrastructure.source.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity(name = "SourceOutboxEventEntity")
@Table(name = "outbox_events", schema = "public")
public class SourceOutboxEventEntity {

    @Id
    @Column(name = "outbox_event_id", nullable = false, updatable = false)
    private UUID outboxEventId;

    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    @Column(name = "event_type", nullable = false, length = 128)
    private String eventType;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public UUID getOutboxEventId() {
        return outboxEventId;
    }

    public UUID getAggregateId() {
        return aggregateId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
