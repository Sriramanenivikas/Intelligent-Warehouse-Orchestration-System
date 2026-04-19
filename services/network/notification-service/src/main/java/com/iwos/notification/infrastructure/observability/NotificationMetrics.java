package com.iwos.notification.infrastructure.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class NotificationMetrics {

    private final Counter notificationsGenerated;
    private final Counter inboundEventsProcessed;
    private final Counter inboundEventsSkipped;
    private final Counter inboundEventsFailed;
    private final Counter outboxPublished;
    private final Counter outboxFailed;

    public NotificationMetrics(MeterRegistry meterRegistry) {
        this.notificationsGenerated = Counter.builder("notification_generated_total")
                .description("Notifications generated")
                .register(meterRegistry);
        this.inboundEventsProcessed = Counter.builder("notification_inbound_events_total")
                .tag("outcome", "processed")
                .description("Inbound scan events processed")
                .register(meterRegistry);
        this.inboundEventsSkipped = Counter.builder("notification_inbound_events_total")
                .tag("outcome", "skipped")
                .description("Inbound scan events skipped")
                .register(meterRegistry);
        this.inboundEventsFailed = Counter.builder("notification_inbound_events_total")
                .tag("outcome", "failed")
                .description("Inbound scan events failed")
                .register(meterRegistry);
        this.outboxPublished = Counter.builder("notification_outbox_published_total")
                .tag("outcome", "published")
                .description("Notification outbox events published")
                .register(meterRegistry);
        this.outboxFailed = Counter.builder("notification_outbox_published_total")
                .tag("outcome", "failed")
                .description("Notification outbox events failed")
                .register(meterRegistry);
    }

    public void incrementNotificationsGenerated(int count) {
        notificationsGenerated.increment(count);
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
