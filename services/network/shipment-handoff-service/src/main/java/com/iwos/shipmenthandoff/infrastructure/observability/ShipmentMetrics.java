package com.iwos.shipmenthandoff.infrastructure.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class ShipmentMetrics {

    private final Counter shipmentsCreated;
    private final Counter shipmentsManifested;
    private final Counter shipmentsDispatched;
    private final Counter shipmentsDelivered;
    private final Counter inboundEventsProcessed;
    private final Counter inboundEventsSkipped;
    private final Counter inboundEventsFailed;
    private final Counter outboxPublished;
    private final Counter outboxFailed;

    public ShipmentMetrics(MeterRegistry registry) {
        this.shipmentsCreated = Counter.builder("shipment_handoff_shipments_created_total")
                .description("Total shipments created")
                .register(registry);

        this.shipmentsManifested = Counter.builder("shipment_handoff_shipments_manifested_total")
                .description("Total shipments manifested")
                .register(registry);

        this.shipmentsDispatched = Counter.builder("shipment_handoff_shipments_dispatched_total")
                .description("Total shipments dispatched")
                .register(registry);

        this.shipmentsDelivered = Counter.builder("shipment_handoff_shipments_delivered_total")
                .description("Total shipments delivered")
                .register(registry);

        this.inboundEventsProcessed = Counter.builder("shipment_handoff_inbound_events_processed_total")
                .description("Total inbound Kafka events processed")
                .register(registry);

        this.inboundEventsSkipped = Counter.builder("shipment_handoff_inbound_events_skipped_total")
                .description("Total inbound Kafka events skipped")
                .register(registry);

        this.inboundEventsFailed = Counter.builder("shipment_handoff_inbound_events_failed_total")
                .description("Total inbound Kafka events failed")
                .register(registry);

        this.outboxPublished = Counter.builder("shipment_handoff_outbox_published_total")
                .tag("outcome", "published")
                .description("Total outbox events published")
                .register(registry);

        this.outboxFailed = Counter.builder("shipment_handoff_outbox_published_total")
                .tag("outcome", "failed")
                .description("Total outbox events failed")
                .register(registry);
    }

    public void incrementShipmentsCreated() { shipmentsCreated.increment(); }
    public void incrementShipmentsManifested() { shipmentsManifested.increment(); }
    public void incrementShipmentsDispatched() { shipmentsDispatched.increment(); }
    public void incrementShipmentsDelivered() { shipmentsDelivered.increment(); }
    public void incrementInboundEventsProcessed() { inboundEventsProcessed.increment(); }
    public void incrementInboundEventsSkipped() { inboundEventsSkipped.increment(); }
    public void incrementInboundEventsFailed() { inboundEventsFailed.increment(); }
    public void incrementOutboxPublished() { outboxPublished.increment(); }
    public void incrementOutboxFailed() { outboxFailed.increment(); }
}
