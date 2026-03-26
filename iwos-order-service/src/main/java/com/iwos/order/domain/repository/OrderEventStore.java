package com.iwos.order.domain.repository;

public interface OrderEventStore {
    // Event store interface for event sourcing
    // append(event), getEvents(aggregateId), getSnapshot(aggregateId)
}
