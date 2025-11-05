package com.iwos.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Base Event Class
 * All domain events inherit from this
 *
 * Architecture: Event-Driven Choreography
 * Pattern: Domain Event
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public abstract class DomainEvent {

    private String eventId;
    private String eventType;
    private Instant timestamp;
    private String aggregateId;
    private String correlationId;  // For distributed tracing
    private String source;  // Service that published event

    /**
     * Factory method to create event with metadata
     */
    public static DomainEvent create(String eventType, String aggregateId, String source) {
        return DomainEvent.builder()
            .eventId(java.util.UUID.randomUUID().toString())
            .eventType(eventType)
            .timestamp(Instant.now())
            .aggregateId(aggregateId)
            .source(source)
            .build();
    }
}
