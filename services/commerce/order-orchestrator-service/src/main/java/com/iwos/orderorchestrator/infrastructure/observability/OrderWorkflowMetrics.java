package com.iwos.orderorchestrator.infrastructure.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class OrderWorkflowMetrics {

    private final MeterRegistry meterRegistry;

    public OrderWorkflowMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public Timer.Sample startWorkflowTimer() {
        return Timer.start(meterRegistry);
    }

    public Timer.Sample startInventoryClientTimer() {
        return Timer.start(meterRegistry);
    }

    public Timer.Sample startPaymentClientTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordWorkflowProcessed(String status, Timer.Sample sample) {
        Counter.builder("iwos.order.workflow.process")
                .description("Processed order workflows by status")
                .tag("status", status)
                .register(meterRegistry)
                .increment();

        sample.stop(Timer.builder("iwos.order.workflow.process.duration")
                .description("Order workflow processing duration by status")
                .tag("status", status)
                .publishPercentileHistogram()
                .register(meterRegistry));
    }

    public void recordInventoryClientCall(String action, String outcome, Timer.Sample sample) {
        Counter.builder("iwos.order.workflow.inventory.client")
                .description("Order workflow inventory client calls by action and outcome")
                .tag("action", action)
                .tag("outcome", outcome)
                .register(meterRegistry)
                .increment();

        sample.stop(Timer.builder("iwos.order.workflow.inventory.client.duration")
                .description("Order workflow inventory client call duration")
                .tag("action", action)
                .tag("outcome", outcome)
                .publishPercentileHistogram()
                .register(meterRegistry));
    }

    public void recordPaymentClientCall(String action, String outcome, Timer.Sample sample) {
        Counter.builder("iwos.order.workflow.payment.client")
                .description("Order workflow payment client calls by action and outcome")
                .tag("action", action)
                .tag("outcome", outcome)
                .register(meterRegistry)
                .increment();

        sample.stop(Timer.builder("iwos.order.workflow.payment.client.duration")
                .description("Order workflow payment client call duration")
                .tag("action", action)
                .tag("outcome", outcome)
                .publishPercentileHistogram()
                .register(meterRegistry));
    }

    public void recordReservationCompensation(int releasedReservations) {
        if (releasedReservations <= 0) {
            return;
        }

        Counter.builder("iwos.order.workflow.reservation.compensation")
                .description("Released reservations during workflow compensation")
                .register(meterRegistry)
                .increment(releasedReservations);
    }

    public void recordKafkaConsume(String outcome) {
        Counter.builder("iwos.order.workflow.kafka.consume")
                .description("Order workflow Kafka consume attempts by outcome")
                .tag("outcome", outcome)
                .register(meterRegistry)
                .increment();
    }

    public void recordOutboxPublish(String status) {
        Counter.builder("iwos.order.workflow.outbox.publish")
                .description("Order workflow outbox publish attempts by status")
                .tag("status", status)
                .register(meterRegistry)
                .increment();
    }
}
