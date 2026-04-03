package com.iwos.inventoryledger.infrastructure.observability;

import com.iwos.inventoryledger.domain.reservation.InventoryReservationStatus;
import com.iwos.inventoryledger.infrastructure.persistence.repository.InventoryOutboxEventRepository;
import com.iwos.inventoryledger.infrastructure.persistence.repository.InventoryReservationRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class InventoryMeterBinder {

    public InventoryMeterBinder(
            MeterRegistry meterRegistry,
            InventoryReservationRepository inventoryReservationRepository,
            InventoryOutboxEventRepository inventoryOutboxEventRepository
    ) {
        for (InventoryReservationStatus status : InventoryReservationStatus.values()) {
            Gauge.builder("iwos.inventory.reservation.total", inventoryReservationRepository, repository -> repository.countByStatus(status))
                    .description("Current inventory reservations by status")
                    .tag("status", status.name().toLowerCase())
                    .baseUnit("reservations")
                    .register(meterRegistry);
        }

        Gauge.builder("iwos.inventory.outbox.pending", inventoryOutboxEventRepository, repository -> repository.countByStatus("PENDING"))
                .description("Pending inventory outbox events")
                .baseUnit("events")
                .register(meterRegistry);
    }
}
