package com.iwos.promiseallocation.infrastructure.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class PromiseAllocationMetrics {

    private final MeterRegistry meterRegistry;

    public PromiseAllocationMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public Timer.Sample startResolveTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordResolve(String outcome, Timer.Sample sample) {
        Counter.builder("iwos.promise.resolve")
                .description("Promise resolution requests by outcome")
                .tag("outcome", outcome)
                .register(meterRegistry)
                .increment();

        sample.stop(Timer.builder("iwos.promise.resolve.duration")
                .description("Promise resolution duration by outcome")
                .tag("outcome", outcome)
                .publishPercentileHistogram()
                .register(meterRegistry));
    }
}
