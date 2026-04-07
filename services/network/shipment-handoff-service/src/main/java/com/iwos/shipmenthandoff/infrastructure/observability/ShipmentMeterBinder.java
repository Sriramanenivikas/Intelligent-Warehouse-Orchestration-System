package com.iwos.shipmenthandoff.infrastructure.observability;

import com.iwos.shipmenthandoff.domain.shipment.ShipmentStatus;
import com.iwos.shipmenthandoff.infrastructure.persistence.repository.ShipmentOutboxEventRepository;
import com.iwos.shipmenthandoff.infrastructure.persistence.repository.ShipmentRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
public class ShipmentMeterBinder implements MeterBinder {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentOutboxEventRepository outboxRepository;

    public ShipmentMeterBinder(
            ShipmentRepository shipmentRepository,
            ShipmentOutboxEventRepository outboxRepository
    ) {
        this.shipmentRepository = shipmentRepository;
        this.outboxRepository = outboxRepository;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        Gauge.builder("shipment_handoff_shipments_created", shipmentRepository,
                        repo -> repo.findByStatusOrderByCreatedAtDesc(ShipmentStatus.CREATED, PageRequest.of(0, 10000)).size())
                .description("Number of shipments in CREATED status")
                .register(registry);

        Gauge.builder("shipment_handoff_shipments_manifested", shipmentRepository,
                        repo -> repo.findByStatusOrderByCreatedAtDesc(ShipmentStatus.MANIFESTED, PageRequest.of(0, 10000)).size())
                .description("Number of shipments in MANIFESTED status")
                .register(registry);

        Gauge.builder("shipment_handoff_shipments_dispatched", shipmentRepository,
                        repo -> repo.findByStatusOrderByCreatedAtDesc(ShipmentStatus.DISPATCHED, PageRequest.of(0, 10000)).size())
                .description("Number of shipments in DISPATCHED status")
                .register(registry);

        Gauge.builder("shipment_handoff_outbox_pending", outboxRepository,
                        repo -> repo.countByStatus("PENDING"))
                .description("Number of pending outbox events")
                .register(registry);
    }
}
