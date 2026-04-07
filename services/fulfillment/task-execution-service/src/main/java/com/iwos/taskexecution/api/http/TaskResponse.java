package com.iwos.taskexecution.api.http;

import com.iwos.taskexecution.domain.task.TaskStatus;
import com.iwos.taskexecution.domain.task.TaskType;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record TaskResponse(
        UUID taskAssignmentId,
        UUID fulfillmentTaskId,
        UUID fulfillmentOrderId,
        UUID orderIntentId,
        TaskType taskType,
        TaskStatus status,
        String nodeId,
        String taskTitle,
        Map<String, Object> taskPayload,
        String workerId,
        Instant claimedAt,
        Instant startedAt,
        Instant completedAt,
        String failureReason,
        int attemptCount,
        Instant createdAt,
        Instant updatedAt
) {}
