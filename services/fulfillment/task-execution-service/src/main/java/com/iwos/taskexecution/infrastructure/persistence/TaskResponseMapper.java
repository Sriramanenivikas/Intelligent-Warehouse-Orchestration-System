package com.iwos.taskexecution.infrastructure.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwos.taskexecution.api.http.TaskResponse;
import com.iwos.taskexecution.infrastructure.persistence.entity.TaskAssignmentEntity;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class TaskResponseMapper {

    private final ObjectMapper objectMapper;

    public TaskResponseMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public TaskResponse toResponse(TaskAssignmentEntity entity) {
        Map<String, Object> payload = null;
        if (entity.getTaskPayload() != null) {
            try {
                payload = objectMapper.readValue(entity.getTaskPayload(), new TypeReference<>() {});
            } catch (Exception e) {
                payload = Map.of("raw", entity.getTaskPayload());
            }
        }

        return new TaskResponse(
                entity.getTaskAssignmentId(),
                entity.getFulfillmentTaskId(),
                entity.getFulfillmentOrderId(),
                entity.getOrderIntentId(),
                entity.getTaskType(),
                entity.getStatus(),
                entity.getNodeId(),
                entity.getTaskTitle(),
                payload,
                entity.getWorkerId(),
                entity.getClaimedAt(),
                entity.getStartedAt(),
                entity.getCompletedAt(),
                entity.getFailureReason(),
                entity.getAttemptCount(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
