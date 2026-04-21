package com.iwos.notification.infrastructure.observability;

import com.iwos.notification.domain.notification.NotificationAudience;
import com.iwos.notification.domain.notification.NotificationStatus;
import com.iwos.notification.infrastructure.persistence.repository.NotificationOutboxEventRepository;
import com.iwos.notification.infrastructure.persistence.repository.NotificationRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class NotificationMeterBinder {

    public NotificationMeterBinder(
            MeterRegistry meterRegistry,
            NotificationRepository notificationRepository,
            NotificationOutboxEventRepository outboxRepository
    ) {
        for (NotificationStatus status : NotificationStatus.values()) {
            Gauge.builder("notification_records_current", notificationRepository,
                            repository -> repository.countByStatus(status))
                    .tag("status", status.name())
                    .description("Notifications by status")
                    .register(meterRegistry);
        }

        for (NotificationAudience audience : NotificationAudience.values()) {
            Gauge.builder("notification_audience_current", notificationRepository,
                            repository -> repository.countByAudience(audience))
                    .tag("audience", audience.name())
                    .description("Notifications by audience")
                    .register(meterRegistry);
        }

        Gauge.builder("notification_outbox_pending_current", outboxRepository,
                        repository -> repository.countByStatus("PENDING"))
                .description("Pending notification outbox events")
                .register(meterRegistry);
    }
}
