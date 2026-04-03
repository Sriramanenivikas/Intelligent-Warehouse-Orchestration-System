package com.iwos.orderintake.infrastructure.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class OrderIntakeMetrics {

    private final MeterRegistry meterRegistry;

    public OrderIntakeMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public Timer.Sample startAcceptTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordAccept(String outcome, Timer.Sample sample) {
        Counter.builder("iwos.order.intake.accept")
                .description("Order intake accept requests by outcome")
                .tag("outcome", outcome)
                .register(meterRegistry)
                .increment();

        sample.stop(Timer.builder("iwos.order.intake.accept.duration")
                .description("Order intake accept duration by outcome")
                .tag("outcome", outcome)
                .publishPercentileHistogram()
                .register(meterRegistry));
    }

    public void recordOutboxPublish(String status) {
        Counter.builder("iwos.order.intake.outbox.publish")
                .description("Order intake outbox publish attempts by status")
                .tag("status", status)
                .register(meterRegistry)
                .increment();
    }
}
