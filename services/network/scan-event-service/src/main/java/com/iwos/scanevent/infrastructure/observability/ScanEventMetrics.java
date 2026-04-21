package com.iwos.scanevent.infrastructure.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class ScanEventMetrics {

    private final Counter trackedShipmentsCreated;
    private final Counter scanEventsNormalized;
    private final Counter inboundEventsProcessed;
    private final Counter inboundEventsSkipped;
    private final Counter inboundEventsFailed;
    private final Counter outboxPublished;
    private final Counter outboxFailed;

    public ScanEventMetrics(MeterRegistry meterRegistry) {
        this.trackedShipmentsCreated = Counter.builder("scan_event_tracked_shipments_created_total")
                .description("Tracked shipments created")
                .register(meterRegistry);
        this.scanEventsNormalized = Counter.builder("scan_event_events_normalized_total")
                .description("Normalized scan events written")
                .register(meterRegistry);
        this.inboundEventsProcessed = Counter.builder("scan_event_inbound_events_total")
                .tag("outcome", "processed")
                .description("Inbound shipment-network events processed")
                .register(meterRegistry);
        this.inboundEventsSkipped = Counter.builder("scan_event_inbound_events_total")
                .tag("outcome", "skipped")
                .description("Inbound shipment-network events skipped")
                .register(meterRegistry);
        this.inboundEventsFailed = Counter.builder("scan_event_inbound_events_total")
                .tag("outcome", "failed")
                .description("Inbound shipment-network events failed")
                .register(meterRegistry);
        this.outboxPublished = Counter.builder("scan_event_outbox_published_total")
                .tag("outcome", "published")
                .description("Scan-event outbox events published")
                .register(meterRegistry);
        this.outboxFailed = Counter.builder("scan_event_outbox_published_total")
                .tag("outcome", "failed")
                .description("Scan-event outbox events failed")
                .register(meterRegistry);
    }

    public void incrementTrackedShipmentsCreated() {
        trackedShipmentsCreated.increment();
    }

    public void incrementScanEventsNormalized() {
        scanEventsNormalized.increment();
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
