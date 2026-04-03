package com.iwos.payment.infrastructure.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class PaymentMetrics {

    private final MeterRegistry meterRegistry;

    public PaymentMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public Timer.Sample startCommandTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordCommand(String operation, String status, Timer.Sample sample) {
        sample.stop(Timer.builder("iwos.payment.command")
                .tag("operation", operation)
                .tag("status", status)
                .register(meterRegistry));
    }

    public void recordOutbox(String status) {
        Counter.builder("iwos.payment.outbox.publish")
                .tag("status", status)
                .register(meterRegistry)
                .increment();
    }
}
