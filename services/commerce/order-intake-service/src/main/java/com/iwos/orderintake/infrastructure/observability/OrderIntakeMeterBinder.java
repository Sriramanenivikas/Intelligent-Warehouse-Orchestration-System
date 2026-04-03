package com.iwos.orderintake.infrastructure.observability;

import com.iwos.orderintake.infrastructure.persistence.repository.OutboxEventRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class OrderIntakeMeterBinder {

    public OrderIntakeMeterBinder(
            MeterRegistry meterRegistry,
            OutboxEventRepository outboxEventRepository
    ) {
        Gauge.builder("iwos.order.intake.outbox.pending", outboxEventRepository, repository -> repository.countByStatus("PENDING"))
                .description("Pending order intake outbox events")
                .baseUnit("events")
                .register(meterRegistry);
    }
}
