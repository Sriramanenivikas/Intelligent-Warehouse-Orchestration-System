package com.iwos.shipmentnetwork.infrastructure.observability;

import com.iwos.shipmentnetwork.domain.network.NetworkShipmentStatus;
import com.iwos.shipmentnetwork.infrastructure.persistence.repository.NetworkOutboxEventRepository;
import com.iwos.shipmentnetwork.infrastructure.persistence.repository.NetworkShipmentRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class ShipmentNetworkMeterBinder {

    public ShipmentNetworkMeterBinder(
            MeterRegistry meterRegistry,
            NetworkShipmentRepository shipmentRepository,
            NetworkOutboxEventRepository outboxRepository
    ) {
        for (NetworkShipmentStatus status : NetworkShipmentStatus.values()) {
            Gauge.builder("shipment_network_shipments_current", shipmentRepository,
                            repository -> repository.countByStatus(status))
                    .tag("status", status.name())
                    .description("Network shipments by status")
                    .register(meterRegistry);
        }

        Gauge.builder("shipment_network_outbox_pending", outboxRepository,
                        repository -> repository.countByStatus("PENDING"))
                .description("Pending shipment-network outbox events")
                .register(meterRegistry);
    }
}
