package com.iwos.shipmentnetwork.infrastructure.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class ShipmentNetworkMetrics {

    private final Counter networkShipmentsCreated;
    private final Counter scanEventsRecorded;
    private final Counter inboundEventsProcessed;
    private final Counter inboundEventsSkipped;
    private final Counter inboundEventsFailed;
    private final Counter outboxPublished;
    private final Counter outboxFailed;

    public ShipmentNetworkMetrics(MeterRegistry meterRegistry) {
        this.networkShipmentsCreated = Counter.builder("shipment_network_shipments_created_total")
                .description("Network shipments created")
                .register(meterRegistry);
        this.scanEventsRecorded = Counter.builder("shipment_network_scan_events_recorded_total")
                .description("Scan events recorded")
                .register(meterRegistry);
        this.inboundEventsProcessed = Counter.builder("shipment_network_inbound_events_total")
                .tag("outcome", "processed")
                .description("Inbound events processed")
                .register(meterRegistry);
        this.inboundEventsSkipped = Counter.builder("shipment_network_inbound_events_total")
                .tag("outcome", "skipped")
                .description("Inbound events skipped")
                .register(meterRegistry);
        this.inboundEventsFailed = Counter.builder("shipment_network_inbound_events_total")
                .tag("outcome", "failed")
                .description("Inbound events failed")
                .register(meterRegistry);
        this.outboxPublished = Counter.builder("shipment_network_outbox_published_total")
                .tag("outcome", "published")
                .description("Outbox events published")
                .register(meterRegistry);
        this.outboxFailed = Counter.builder("shipment_network_outbox_published_total")
                .tag("outcome", "failed")
                .description("Outbox events failed")
                .register(meterRegistry);
    }

    public void incrementNetworkShipmentsCreated() {
        networkShipmentsCreated.increment();
    }

    public void incrementScanEventsRecorded() {
        scanEventsRecorded.increment();
    }

    public void incrementInboundEventsProcessed() {
        inboundEventsProcessed.increment();
    }

    public void incrementInboundEventsSkipped() {
        inboundEventsSkipped.increment();
    }

    public void incrementInboundEventsFailed() {
        inboundEventsFailed.increment();
    }

    public void incrementOutboxPublished() {
        outboxPublished.increment();
    }

    public void incrementOutboxFailed() {
        outboxFailed.increment();
    }
}
