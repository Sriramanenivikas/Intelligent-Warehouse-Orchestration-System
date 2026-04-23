package com.iwos.taskexecution.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwos.taskexecution.api.http.TaskResponse;
import com.iwos.taskexecution.domain.task.TaskAlreadyClaimedException;
import com.iwos.taskexecution.domain.task.TaskNotFoundException;
import com.iwos.taskexecution.domain.task.TaskStateException;
import com.iwos.taskexecution.domain.task.TaskStatus;
import com.iwos.taskexecution.domain.task.TaskType;
import com.iwos.taskexecution.infrastructure.observability.TaskExecutionMetrics;
import com.iwos.taskexecution.infrastructure.persistence.TaskResponseMapper;
import com.iwos.taskexecution.infrastructure.persistence.entity.TaskAssignmentEntity;
import com.iwos.taskexecution.infrastructure.persistence.entity.TaskOutboxEventEntity;
import com.iwos.taskexecution.infrastructure.persistence.repository.TaskAssignmentRepository;
import com.iwos.taskexecution.infrastructure.persistence.repository.TaskOutboxEventRepository;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskExecutionService {

    private static final Logger log = LoggerFactory.getLogger(TaskExecutionService.class);

    private final TaskAssignmentRepository taskRepository;
    private final TaskOutboxEventRepository outboxRepository;
    private final TaskResponseMapper mapper;
    private final TaskExecutionMetrics metrics;
    private final ObjectMapper objectMapper;

    public TaskExecutionService(
            TaskAssignmentRepository taskRepository,
            TaskOutboxEventRepository outboxRepository,
            TaskResponseMapper mapper,
            TaskExecutionMetrics metrics,
            ObjectMapper objectMapper
    ) {
        this.taskRepository = taskRepository;
        this.outboxRepository = outboxRepository;
        this.mapper = mapper;
        this.metrics = metrics;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public TaskResponse claimTask(UUID taskAssignmentId, String workerId) {
        TaskAssignmentEntity task = taskRepository.findById(taskAssignmentId)
                .orElseThrow(() -> new TaskNotFoundException(taskAssignmentId));

        if (task.getStatus() == TaskStatus.CLAIMED || task.getStatus() == TaskStatus.IN_PROGRESS) {
            if (!workerId.equals(task.getWorkerId())) {
                throw new TaskAlreadyClaimedException(taskAssignmentId, task.getWorkerId());
            }
            // Re-claim by same worker is idempotent
            return mapper.toResponse(task);
        }

        if (task.getStatus() != TaskStatus.READY) {
            throw new TaskStateException(taskAssignmentId, task.getStatus(), "claim");
        }

        Instant now = Instant.now();
        task.setStatus(TaskStatus.CLAIMED);
        task.setWorkerId(workerId);
        task.setClaimedAt(now);
        task.setUpdatedAt(now);
        task.setAttemptCount(task.getAttemptCount() + 1);

        task = taskRepository.save(task);
        metrics.incrementTasksClaimed();

        log.info("Task claimed: taskAssignmentId={}, workerId={}", taskAssignmentId, workerId);
        return mapper.toResponse(task);
    }

    @Transactional
    public TaskResponse startTask(UUID taskAssignmentId, String workerId) {
        TaskAssignmentEntity task = taskRepository.findById(taskAssignmentId)
                .orElseThrow(() -> new TaskNotFoundException(taskAssignmentId));

        if (task.getStatus() == TaskStatus.IN_PROGRESS && workerId.equals(task.getWorkerId())) {
            return mapper.toResponse(task);
        }

        if (task.getStatus() != TaskStatus.CLAIMED) {
            throw new TaskStateException(taskAssignmentId, task.getStatus(), "start");
        }

        if (!workerId.equals(task.getWorkerId())) {
            throw new TaskAlreadyClaimedException(taskAssignmentId, task.getWorkerId());
        }

        Instant now = Instant.now();
        task.setStatus(TaskStatus.IN_PROGRESS);
        task.setStartedAt(now);
        task.setUpdatedAt(now);

        task = taskRepository.save(task);
        metrics.incrementTasksStarted();

        log.info("Task started: taskAssignmentId={}, workerId={}", taskAssignmentId, workerId);
        return mapper.toResponse(task);
    }

    @Transactional
    public TaskResponse completeTask(UUID taskAssignmentId, String workerId, String notes) {
        TaskAssignmentEntity task = taskRepository.findById(taskAssignmentId)
                .orElseThrow(() -> new TaskNotFoundException(taskAssignmentId));

        if (task.getStatus() == TaskStatus.COMPLETED) {
            return mapper.toResponse(task);
        }

        if (task.getStatus() != TaskStatus.CLAIMED && task.getStatus() != TaskStatus.IN_PROGRESS) {
            throw new TaskStateException(taskAssignmentId, task.getStatus(), "complete");
        }

        if (!workerId.equals(task.getWorkerId())) {
            throw new TaskAlreadyClaimedException(taskAssignmentId, task.getWorkerId());
        }

        Instant now = Instant.now();
        task.setStatus(TaskStatus.COMPLETED);
        task.setCompletedAt(now);
        task.setUpdatedAt(now);

        task = taskRepository.save(task);
        metrics.incrementTasksCompleted();

        // Create outbox event
        createOutboxEvent(task, "task-execution.task-completed.v1", notes);

        // Check if we need to unblock PACK task
        checkAndUnblockPackTask(task);

        log.info("Task completed: taskAssignmentId={}, workerId={}, type={}",
                taskAssignmentId, workerId, task.getTaskType());
        return mapper.toResponse(task);
    }

    @Transactional
    public TaskResponse failTask(UUID taskAssignmentId, String workerId, String reason) {
        TaskAssignmentEntity task = taskRepository.findById(taskAssignmentId)
                .orElseThrow(() -> new TaskNotFoundException(taskAssignmentId));

        if (task.getStatus() == TaskStatus.FAILED) {
            return mapper.toResponse(task);
        }

        if (task.getStatus() != TaskStatus.CLAIMED && task.getStatus() != TaskStatus.IN_PROGRESS) {
            throw new TaskStateException(taskAssignmentId, task.getStatus(), "fail");
        }

        if (!workerId.equals(task.getWorkerId())) {
            throw new TaskAlreadyClaimedException(taskAssignmentId, task.getWorkerId());
        }

        Instant now = Instant.now();
        task.setStatus(TaskStatus.FAILED);
        task.setFailureReason(reason);
        task.setUpdatedAt(now);

        task = taskRepository.save(task);
        metrics.incrementTasksFailed();

        // Create outbox event
        createOutboxEvent(task, "task-execution.task-failed.v1", reason);

        log.warn("Task failed: taskAssignmentId={}, workerId={}, reason={}",
                taskAssignmentId, workerId, reason);
        return mapper.toResponse(task);
    }

    private void checkAndUnblockPackTask(TaskAssignmentEntity completedTask) {
        if (completedTask.getTaskType() != TaskType.PICK) {
            return;
        }

        UUID fulfillmentOrderId = completedTask.getFulfillmentOrderId();

        // Count remaining non-completed PICK tasks
        long pendingPicks = taskRepository.countByFulfillmentOrderIdAndTaskTypeAndStatus(
                fulfillmentOrderId, TaskType.PICK, TaskStatus.READY);
        long claimedPicks = taskRepository.countByFulfillmentOrderIdAndTaskTypeAndStatus(
                fulfillmentOrderId, TaskType.PICK, TaskStatus.CLAIMED);
        long inProgressPicks = taskRepository.countByFulfillmentOrderIdAndTaskTypeAndStatus(
                fulfillmentOrderId, TaskType.PICK, TaskStatus.IN_PROGRESS);
        long blockedPicks = taskRepository.countByFulfillmentOrderIdAndTaskTypeAndStatus(
                fulfillmentOrderId, TaskType.PICK, TaskStatus.BLOCKED);

        long incompletePicks = pendingPicks + claimedPicks + inProgressPicks + blockedPicks;

        if (incompletePicks == 0) {
            // All PICKs done, unblock PACK task
            List<TaskAssignmentEntity> packTasks = taskRepository.findByFulfillmentOrderIdAndTaskType(
                    fulfillmentOrderId, TaskType.PACK);

            for (TaskAssignmentEntity packTask : packTasks) {
                if (packTask.getStatus() == TaskStatus.BLOCKED && packTask.getWorkerId() == null) {
                    Instant now = Instant.now();
                    packTask.setStatus(TaskStatus.READY);
                    packTask.setUpdatedAt(now);
                    taskRepository.save(packTask);

                    log.info("All PICK tasks completed. PACK task now available: fulfillmentOrderId={}, packTaskId={}",
                            fulfillmentOrderId, packTask.getTaskAssignmentId());

                    createOutboxEvent(packTask, "task-execution.pack-unblocked.v1", null);
                }
            }
        }
    }

    private void createOutboxEvent(TaskAssignmentEntity task, String eventType, String notes) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("taskAssignmentId", task.getTaskAssignmentId().toString());
            payload.put("fulfillmentTaskId", task.getFulfillmentTaskId().toString());
            payload.put("fulfillmentOrderId", task.getFulfillmentOrderId().toString());
            payload.put("orderIntentId", task.getOrderIntentId().toString());
            payload.put("taskType", task.getTaskType().name());
            payload.put("status", task.getStatus().name());
            payload.put("nodeId", task.getNodeId());
            payload.put("workerId", task.getWorkerId());
            payload.put("occurredAt", Instant.now().toString());
            payload.put("eventType", eventType);
            payload.put("aggregateType", "TASK_ASSIGNMENT");
            payload.put("aggregateId", task.getTaskAssignmentId().toString());

            if (notes != null) {
                payload.put("notes", notes);
            }
            if (task.getFailureReason() != null) {
                payload.put("failureReason", task.getFailureReason());
            }

            Instant now = Instant.now();
            TaskOutboxEventEntity outbox = new TaskOutboxEventEntity();
            outbox.setOutboxEventId(UUID.randomUUID());
            outbox.setAggregateType("TASK_ASSIGNMENT");
            outbox.setAggregateId(task.getTaskAssignmentId());
            outbox.setEventType(eventType);
            outbox.setStatus("PENDING");
            outbox.setPayload(objectMapper.writeValueAsString(payload));
            outbox.setCreatedAt(now);
            outbox.setUpdatedAt(now);

            outboxRepository.save(outbox);
        } catch (Exception e) {
            log.error("Failed to create outbox event for task: {}", task.getTaskAssignmentId(), e);
        }
    }
}
