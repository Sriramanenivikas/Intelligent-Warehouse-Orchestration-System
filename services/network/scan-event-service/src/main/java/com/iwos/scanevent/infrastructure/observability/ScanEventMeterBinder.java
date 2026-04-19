package com.iwos.scanevent.infrastructure.observability;

import com.iwos.scanevent.domain.scan.TrackingStatus;
import com.iwos.scanevent.infrastructure.persistence.repository.ScanEventOutboxEventRepository;
import com.iwos.scanevent.infrastructure.persistence.repository.TrackedShipmentRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class ScanEventMeterBinder {

    public ScanEventMeterBinder(
            MeterRegistry meterRegistry,
            TrackedShipmentRepository trackedShipmentRepository,
            ScanEventOutboxEventRepository outboxRepository
    ) {
        for (TrackingStatus status : TrackingStatus.values()) {
            Gauge.builder("scan_event_tracked_shipments_current", trackedShipmentRepository,
                            repository -> repository.countByCurrentStatus(status))
                    .tag("status", status.name())
                    .description("Tracked shipments by current status")
                    .register(meterRegistry);
        }

        Gauge.builder("scan_event_outbox_pending_current", outboxRepository,
                        repository -> repository.countByStatus("PENDING"))
                .description("Pending scan-event outbox events")
                .register(meterRegistry);
    }
}
