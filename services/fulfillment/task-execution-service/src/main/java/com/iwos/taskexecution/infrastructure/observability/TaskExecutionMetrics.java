package com.iwos.taskexecution.infrastructure.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class TaskExecutionMetrics {

    private final Counter tasksIngested;
    private final Counter tasksClaimed;
    private final Counter tasksStarted;
    private final Counter tasksCompleted;
    private final Counter tasksFailed;
    private final Counter inboundEventsProcessed;
    private final Counter inboundEventsSkipped;
    private final Counter inboundEventsFailed;
    private final Counter outboxPublished;
    private final Counter outboxFailed;

    public TaskExecutionMetrics(MeterRegistry registry) {
        this.tasksIngested = Counter.builder("task_execution_tasks_ingested_total")
                .description("Total tasks ingested from warehouse")
                .register(registry);

        this.tasksClaimed = Counter.builder("task_execution_tasks_claimed_total")
                .description("Total tasks claimed by workers")
                .register(registry);

        this.tasksStarted = Counter.builder("task_execution_tasks_started_total")
                .description("Total tasks started")
                .register(registry);

        this.tasksCompleted = Counter.builder("task_execution_tasks_completed_total")
                .description("Total tasks completed")
                .register(registry);

        this.tasksFailed = Counter.builder("task_execution_tasks_failed_total")
                .description("Total tasks failed")
                .register(registry);

        this.inboundEventsProcessed = Counter.builder("task_execution_inbound_events_processed_total")
                .description("Total inbound Kafka events processed")
                .register(registry);

        this.inboundEventsSkipped = Counter.builder("task_execution_inbound_events_skipped_total")
                .description("Total inbound Kafka events skipped")
                .register(registry);

        this.inboundEventsFailed = Counter.builder("task_execution_inbound_events_failed_total")
                .description("Total inbound Kafka events failed")
                .register(registry);

        this.outboxPublished = Counter.builder("task_execution_outbox_published_total")
                .tag("outcome", "published")
                .description("Total outbox events published")
                .register(registry);

        this.outboxFailed = Counter.builder("task_execution_outbox_published_total")
                .tag("outcome", "failed")
                .description("Total outbox events failed to publish")
                .register(registry);
    }

    public void incrementTasksIngested() {
        tasksIngested.increment();
    }

    public void incrementTasksClaimed() {
        tasksClaimed.increment();
    }

    public void incrementTasksStarted() {
        tasksStarted.increment();
    }

    public void incrementTasksCompleted() {
        tasksCompleted.increment();
    }

    public void incrementTasksFailed() {
        tasksFailed.increment();
    }

    public void incrementInboundEventsProcessed() {
        inboundEventsProcessed.increment();
    }

    public void incrementInboundEventsSkipped() {
        inboundEventsSkipped.increment();
    }

    public void incrementInboundEventsFailed() {
        inboundEventsFailed.increment();
    }

    public void incrementOutboxPublished() {
        outboxPublished.increment();
    }

    public void incrementOutboxFailed() {
        outboxFailed.increment();
    }
}
