package com.iwos.inventoryledger.infrastructure.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class InventoryMetrics {

    private final MeterRegistry meterRegistry;

    public InventoryMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public Timer.Sample startCommandTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordCommand(String operation, String outcome, Timer.Sample sample) {
        Counter.builder("iwos.inventory.command")
                .description("Inventory commands by operation and outcome")
                .tag("operation", operation)
                .tag("outcome", outcome)
                .register(meterRegistry)
                .increment();

        sample.stop(Timer.builder("iwos.inventory.command.duration")
                .description("Inventory command duration by operation and outcome")
                .tag("operation", operation)
                .tag("outcome", outcome)
                .publishPercentileHistogram()
                .register(meterRegistry));
    }

    public void recordOutboxPublish(String status) {
        Counter.builder("iwos.inventory.outbox.publish")
                .description("Inventory outbox publish attempts by status")
                .tag("status", status)
                .register(meterRegistry)
                .increment();
    }
}
