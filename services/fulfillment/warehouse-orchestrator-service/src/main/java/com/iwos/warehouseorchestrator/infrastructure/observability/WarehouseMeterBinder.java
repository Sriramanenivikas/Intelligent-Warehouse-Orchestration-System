package com.iwos.warehouseorchestrator.infrastructure.observability;

import com.iwos.warehouseorchestrator.domain.fulfillment.FulfillmentOrderStatus;
import com.iwos.warehouseorchestrator.infrastructure.persistence.repository.FulfillmentOrderRepository;
import com.iwos.warehouseorchestrator.infrastructure.persistence.repository.WarehouseOutboxEventRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.stereotype.Component;

@Component
public class WarehouseMeterBinder implements MeterBinder {

    private final FulfillmentOrderRepository fulfillmentOrderRepository;
    private final WarehouseOutboxEventRepository warehouseOutboxEventRepository;

    public WarehouseMeterBinder(
            FulfillmentOrderRepository fulfillmentOrderRepository,
            WarehouseOutboxEventRepository warehouseOutboxEventRepository
    ) {
        this.fulfillmentOrderRepository = fulfillmentOrderRepository;
        this.warehouseOutboxEventRepository = warehouseOutboxEventRepository;
    }

    @Override
    public void bindTo(MeterRegistry meterRegistry) {
        Gauge.builder("warehouse.fulfillment_orders.total",
                        fulfillmentOrderRepository,
                        repo -> repo.countByStatus(FulfillmentOrderStatus.TASKS_CREATED))
                .tag("status", "TASKS_CREATED")
                .description("Current fulfillment orders by status")
                .register(meterRegistry);

        Gauge.builder("warehouse.outbox_events.pending",
                        warehouseOutboxEventRepository,
                        repo -> repo.countByStatus("PENDING"))
                .description("Pending warehouse outbox events")
                .register(meterRegistry);
    }
}
