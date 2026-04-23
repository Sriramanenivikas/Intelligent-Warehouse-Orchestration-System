package com.iwos.taskexecution.infrastructure.observability;

import com.iwos.taskexecution.infrastructure.persistence.repository.TaskAssignmentRepository;
import com.iwos.taskexecution.infrastructure.persistence.repository.TaskOutboxEventRepository;
import com.iwos.taskexecution.domain.task.TaskStatus;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.stereotype.Component;

@Component
public class TaskExecutionMeterBinder implements MeterBinder {

    private final TaskAssignmentRepository taskRepository;
    private final TaskOutboxEventRepository outboxRepository;

    public TaskExecutionMeterBinder(
            TaskAssignmentRepository taskRepository,
            TaskOutboxEventRepository outboxRepository
    ) {
        this.taskRepository = taskRepository;
        this.outboxRepository = outboxRepository;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        Gauge.builder("task_execution_tasks_current", taskRepository,
                        repo -> repo.findByStatusOrderBySourceCreatedAtAsc(
                                TaskStatus.READY,
                                org.springframework.data.domain.PageRequest.of(0, 10000)
                        ).size())
                .tag("status", "READY")
                .description("Number of tasks in READY status")
                .register(registry);

        Gauge.builder("task_execution_tasks_current", taskRepository,
                        repo -> repo.findByStatusOrderBySourceCreatedAtAsc(
                                TaskStatus.BLOCKED,
                                org.springframework.data.domain.PageRequest.of(0, 10000)
                        ).size())
                .tag("status", "BLOCKED")
                .description("Number of tasks in BLOCKED status")
                .register(registry);

        Gauge.builder("task_execution_tasks_current", taskRepository,
                        repo -> repo.findByStatusOrderBySourceCreatedAtAsc(
                                TaskStatus.CLAIMED,
                                org.springframework.data.domain.PageRequest.of(0, 10000)
                        ).size())
                .tag("status", "CLAIMED")
                .description("Number of tasks in CLAIMED status")
                .register(registry);

        Gauge.builder("task_execution_tasks_current", taskRepository,
                        repo -> repo.findByStatusOrderBySourceCreatedAtAsc(
                                TaskStatus.IN_PROGRESS,
                                org.springframework.data.domain.PageRequest.of(0, 10000)
                        ).size())
                .tag("status", "IN_PROGRESS")
                .description("Number of tasks in IN_PROGRESS status")
                .register(registry);

        Gauge.builder("task_execution_outbox_pending", outboxRepository,
                        repo -> repo.countByStatus("PENDING"))
                .description("Number of pending outbox events")
                .register(registry);
    }
}
