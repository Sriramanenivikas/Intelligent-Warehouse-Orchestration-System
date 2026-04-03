package com.iwos.promiseallocation.infrastructure.observability;

import com.iwos.promiseallocation.infrastructure.persistence.repository.NodeProfileRepository;
import com.iwos.promiseallocation.infrastructure.persistence.repository.PromiseEvaluationRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class PromiseAllocationMeterBinder {

    public PromiseAllocationMeterBinder(
            MeterRegistry meterRegistry,
            NodeProfileRepository nodeProfileRepository,
            PromiseEvaluationRepository promiseEvaluationRepository
    ) {
        Gauge.builder("iwos.promise.nodes.active", nodeProfileRepository, NodeProfileRepository::countByActiveTrue)
                .description("Active promise allocation nodes")
                .baseUnit("nodes")
                .register(meterRegistry);

        Gauge.builder("iwos.promise.evaluations.total", promiseEvaluationRepository, PromiseEvaluationRepository::count)
                .description("Persisted promise evaluations")
                .baseUnit("evaluations")
                .register(meterRegistry);
    }
}
