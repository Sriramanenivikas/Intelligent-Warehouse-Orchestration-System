package com.iwos.warehouseorchestrator.infrastructure.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class WarehouseOrchestratorMetrics {

    private final Counter fulfillmentOrdersCreated;
    private final Counter fulfillmentTasksCreated;
    private final Counter inboundEventsProcessed;
    private final Counter inboundEventsSkipped;
    private final Counter inboundEventsFailed;
    private final Counter outboxPublished;
    private final Counter outboxFailed;
    private final Counter taskStateUpdatesReceived;
    private final Counter taskStateUpdatesApplied;
    private final Counter taskStateUpdatesFailed;

    public WarehouseOrchestratorMetrics(MeterRegistry meterRegistry) {
        this.fulfillmentOrdersCreated = Counter.builder("warehouse.fulfillment_orders.created")
                .description("Total fulfillment orders created")
                .register(meterRegistry);

        this.fulfillmentTasksCreated = Counter.builder("warehouse.fulfillment_tasks.created")
                .description("Total fulfillment tasks created")
                .register(meterRegistry);

        this.inboundEventsProcessed = Counter.builder("warehouse.inbound_events.processed")
                .description("Inbound Kafka events successfully processed")
                .register(meterRegistry);

        this.inboundEventsSkipped = Counter.builder("warehouse.inbound_events.skipped")
                .description("Inbound Kafka events skipped (non-matching event type)")
                .register(meterRegistry);

        this.inboundEventsFailed = Counter.builder("warehouse.inbound_events.failed")
                .description("Inbound Kafka events that failed processing")
                .register(meterRegistry);

        this.outboxPublished = Counter.builder("warehouse.outbox.published")
                .tag("outcome", "published")
                .description("Outbox events published to Kafka")
                .register(meterRegistry);

        this.outboxFailed = Counter.builder("warehouse.outbox.published")
                .tag("outcome", "failed")
                .description("Outbox events that failed Kafka publish")
                .register(meterRegistry);

        this.taskStateUpdatesReceived = Counter.builder("warehouse.task_state_updates.received")
                .description("Task state update events received from task-execution")
                .register(meterRegistry);

        this.taskStateUpdatesApplied = Counter.builder("warehouse.task_state_updates.applied")
                .description("Task state updates successfully applied")
                .register(meterRegistry);

        this.taskStateUpdatesFailed = Counter.builder("warehouse.task_state_updates.failed")
                .description("Task state updates that failed")
                .register(meterRegistry);
    }

    public void recordFulfillmentOrderCreated() {
        fulfillmentOrdersCreated.increment();
    }

    public void recordFulfillmentTasksCreated(int count) {
        fulfillmentTasksCreated.increment(count);
    }

    public void recordInboundProcessed() {
        inboundEventsProcessed.increment();
    }

    public void recordInboundSkipped() {
        inboundEventsSkipped.increment();
    }

    public void recordInboundFailed() {
        inboundEventsFailed.increment();
    }

    public void recordOutboxPublish(String outcome) {
        if ("published".equals(outcome)) {
            outboxPublished.increment();
        } else {
            outboxFailed.increment();
        }
    }

    public void recordTaskStateUpdateReceived() {
        taskStateUpdatesReceived.increment();
    }

    public void recordTaskStateUpdateApplied() {
        taskStateUpdatesApplied.increment();
    }

    public void recordTaskStateUpdateFailed() {
        taskStateUpdatesFailed.increment();
    }
}

