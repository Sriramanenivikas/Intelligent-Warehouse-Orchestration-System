package com.iwos.orderorchestrator.infrastructure.observability;

import com.iwos.orderorchestrator.domain.workflow.OrderWorkflowStatus;
import com.iwos.orderorchestrator.infrastructure.persistence.repository.OrderOrchestratorOutboxEventRepository;
import com.iwos.orderorchestrator.infrastructure.persistence.repository.OrderWorkflowRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class OrderWorkflowMeterBinder {

    public OrderWorkflowMeterBinder(
            MeterRegistry meterRegistry,
            OrderWorkflowRepository orderWorkflowRepository,
            OrderOrchestratorOutboxEventRepository outboxEventRepository
    ) {
        for (OrderWorkflowStatus status : OrderWorkflowStatus.values()) {
            Gauge.builder("iwos.order.workflow.total", orderWorkflowRepository, repository -> repository.countByStatus(status))
                    .description("Current order workflow rows by status")
                    .tag("status", status.name().toLowerCase())
                    .baseUnit("workflows")
                    .register(meterRegistry);
        }

        Gauge.builder("iwos.order.workflow.outbox.pending", outboxEventRepository, repository -> repository.countByStatus("PENDING"))
                .description("Pending order workflow outbox events")
                .baseUnit("events")
                .register(meterRegistry);
    }
}
