package com.iwos.eventsourcing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwos.event.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * EVENT STORE - Core of Event Sourcing
 *
 * Responsibilities:
 * - Store all domain events (append-only)
 * - Retrieve event history for an aggregate
 * - Enable event replay for state reconstruction
 *
 * Benefits:
 * - Complete audit trail (cannot alter history)
 * - Temporal queries (what was the state at time T?)
 * - Rebuild entire system from events
 * - Debug by replaying events
 *
 * Schema:
 * CREATE TABLE event_store (
 *   event_id VARCHAR(36) PRIMARY KEY,
 *   aggregate_type VARCHAR(50),  -- 'Order', 'Inventory'
 *   aggregate_id VARCHAR(36),    -- order-123
 *   event_type VARCHAR(100),     -- 'OrderCreated'
 *   event_data JSONB,            -- Full event payload
 *   metadata JSONB,              -- User, IP, etc.
 *   version BIGINT,              -- For ordering
 *   occurred_at TIMESTAMP,
 *   INDEX idx_aggregate (aggregate_type, aggregate_id, version)
 * )
 *
 * This is IMMUTABLE - no updates or deletes!
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class EventStore {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Save event to event store
     *
     * Events are NEVER updated or deleted (append-only log)
     *
     * @param event Domain event to store
     */
    public void save(DomainEvent event) {
        log.info("💾 Saving event to event store: {} (aggregate: {})",
            event.getEventType(), event.getAggregateId());

        try {
            String eventData = objectMapper.writeValueAsString(event);

            String sql = """
                INSERT INTO event_store (
                    event_id, aggregate_type, aggregate_id, event_type,
                    event_data, version, occurred_at
                ) VALUES (?, ?, ?, ?, ?::jsonb, ?, ?)
                """;

            // Note: DomainEvent doesn't have aggregateType, version, occurredAt
            // Using null for missing fields - subclasses should use the overloaded method
            jdbcTemplate.update(sql,
                event.getEventId() != null ? event.getEventId() : UUID.randomUUID().toString(),
                "Unknown", // aggregateType not in DomainEvent
                event.getAggregateId(),
                event.getEventType(),
                eventData,
                1L, // default version
                Timestamp.from(event.getTimestamp() != null ? event.getTimestamp() : Instant.now())
            );

            log.info("✅ Event saved successfully");

        } catch (Exception e) {
            log.error("❌ Failed to save event to event store", e);
            throw new EventStoreException("Failed to save event", e);
        }
    }

    /**
     * Save event with all metadata (overloaded for events with full metadata)
     *
     * @param eventId Event ID
     * @param aggregateType Aggregate type (e.g., "Order")
     * @param aggregateId Aggregate ID
     * @param eventType Event type (e.g., "OrderCreated")
     * @param version Event version
     * @param occurredAt Timestamp when event occurred
     * @param eventData Full event data as object
     */
    public void save(String eventId, String aggregateType, String aggregateId,
                     String eventType, Long version, Instant occurredAt, Object eventData) {
        log.info("💾 Saving event to event store: {} (aggregate: {})", eventType, aggregateId);

        try {
            String eventJson = objectMapper.writeValueAsString(eventData);

            String sql = """
                INSERT INTO event_store (
                    event_id, aggregate_type, aggregate_id, event_type,
                    event_data, version, occurred_at
                ) VALUES (?, ?, ?, ?, ?::jsonb, ?, ?)
                """;

            jdbcTemplate.update(sql,
                eventId != null ? eventId : UUID.randomUUID().toString(),
                aggregateType,
                aggregateId,
                eventType,
                eventJson,
                version,
                Timestamp.from(occurredAt)
            );

            log.info("✅ Event saved successfully");

        } catch (Exception e) {
            log.error("❌ Failed to save event to event store", e);
            throw new EventStoreException("Failed to save event", e);
        }
    }

    /**
     * Get all events for an aggregate (in order)
     *
     * Use case: Reconstruct current state from history
     *
     * Example:
     *   Order order-123 has events:
     *   1. OrderCreated
     *   2. InventoryReserved
     *   3. WarehouseAllocated
     *   4. OrderShipped
     *
     *   Replay all 4 events → Get current order state
     *
     * @param aggregateId The aggregate ID
     * @return List of events in chronological order
     */
    public List<StoredEvent> getEventsForAggregate(String aggregateId) {
        log.info("📜 Retrieving event history for aggregate: {}", aggregateId);

        String sql = """
            SELECT event_id, aggregate_type, aggregate_id, event_type,
                   event_data, version, occurred_at
            FROM event_store
            WHERE aggregate_id = ?
            ORDER BY version ASC
            """;

        List<StoredEvent> events = jdbcTemplate.query(sql,
            (rs, rowNum) -> StoredEvent.builder()
                .eventId(rs.getString("event_id"))
                .aggregateType(rs.getString("aggregate_type"))
                .aggregateId(rs.getString("aggregate_id"))
                .eventType(rs.getString("event_type"))
                .eventData(rs.getString("event_data"))
                .version(rs.getLong("version"))
                .occurredAt(rs.getTimestamp("occurred_at").toInstant())
                .build(),
            aggregateId
        );

        log.info("📚 Found {} events for aggregate: {}", events.size(), aggregateId);

        return events;
    }

    /**
     * Get events by type within time range
     *
     * Use case: "Show me all OrderCreated events today"
     */
    public List<StoredEvent> getEventsByType(String eventType, Instant from, Instant to) {
        log.info("🔍 Querying events: type={}, from={}, to={}", eventType, from, to);

        String sql = """
            SELECT event_id, aggregate_type, aggregate_id, event_type,
                   event_data, version, occurred_at
            FROM event_store
            WHERE event_type = ?
              AND occurred_at BETWEEN ? AND ?
            ORDER BY occurred_at DESC
            """;

        return jdbcTemplate.query(sql,
            (rs, rowNum) -> StoredEvent.builder()
                .eventId(rs.getString("event_id"))
                .aggregateType(rs.getString("aggregate_type"))
                .aggregateId(rs.getString("aggregate_id"))
                .eventType(rs.getString("event_type"))
                .eventData(rs.getString("event_data"))
                .version(rs.getLong("version"))
                .occurredAt(rs.getTimestamp("occurred_at").toInstant())
                .build(),
            eventType,
            Timestamp.from(from),
            Timestamp.from(to)
        );
    }

    /**
     * Rebuild aggregate from events (Event Replay)
     *
     * This is the MAGIC of Event Sourcing!
     *
     * Example:
     *   Current state in DB: Order status = DELIVERED
     *   Question: What was the state 2 hours ago?
     *
     *   Answer: Replay all events up to 2 hours ago
     *   Result: Order status = PACKED
     *
     * @param aggregateId The aggregate to rebuild
     * @param pointInTime Time to rebuild state at
     * @return Events up to that point in time
     */
    public List<StoredEvent> getEventsUpToTime(String aggregateId, Instant pointInTime) {
        log.info("⏰ Time-travel query: Rebuilding {} at {}", aggregateId, pointInTime);

        String sql = """
            SELECT event_id, aggregate_type, aggregate_id, event_type,
                   event_data, version, occurred_at
            FROM event_store
            WHERE aggregate_id = ?
              AND occurred_at <= ?
            ORDER BY version ASC
            """;

        List<StoredEvent> events = jdbcTemplate.query(sql,
            (rs, rowNum) -> StoredEvent.builder()
                .eventId(rs.getString("event_id"))
                .aggregateType(rs.getString("aggregate_type"))
                .aggregateId(rs.getString("aggregate_id"))
                .eventType(rs.getString("event_type"))
                .eventData(rs.getString("event_data"))
                .version(rs.getLong("version"))
                .occurredAt(rs.getTimestamp("occurred_at").toInstant())
                .build(),
            aggregateId,
            Timestamp.from(pointInTime)
        );

        log.info("🕰️ Found {} events up to {}", events.size(), pointInTime);

        return events;
    }

    /**
     * Get event statistics
     *
     * Use case: Monitoring, analytics
     */
    public EventStoreStats getStats() {
        String sql = """
            SELECT
                COUNT(*) as total_events,
                COUNT(DISTINCT aggregate_id) as total_aggregates,
                MIN(occurred_at) as first_event,
                MAX(occurred_at) as last_event
            FROM event_store
            """;

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) ->
            EventStoreStats.builder()
                .totalEvents(rs.getLong("total_events"))
                .totalAggregates(rs.getLong("total_aggregates"))
                .firstEvent(rs.getTimestamp("first_event").toInstant())
                .lastEvent(rs.getTimestamp("last_event").toInstant())
                .build()
        );
    }
}

/**
 * Stored event DTO
 */
@lombok.Data
@lombok.Builder
class StoredEvent {
    private String eventId;
    private String aggregateType;
    private String aggregateId;
    private String eventType;
    private String eventData;  // JSON
    private Long version;
    private Instant occurredAt;
}

/**
 * Event store statistics
 */
@lombok.Data
@lombok.Builder
class EventStoreStats {
    private Long totalEvents;
    private Long totalAggregates;
    private Instant firstEvent;
    private Instant lastEvent;
}

/**
 * Event store exception
 */
class EventStoreException extends RuntimeException {
    public EventStoreException(String message, Throwable cause) {
        super(message, cause);
    }
}
