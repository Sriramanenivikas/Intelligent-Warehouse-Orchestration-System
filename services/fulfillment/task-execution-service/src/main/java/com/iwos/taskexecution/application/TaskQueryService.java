package com.iwos.taskexecution.application;

import com.iwos.taskexecution.api.http.TaskResponse;
import com.iwos.taskexecution.domain.task.TaskNotFoundException;
import com.iwos.taskexecution.domain.task.TaskStatus;
import com.iwos.taskexecution.infrastructure.persistence.TaskResponseMapper;
import com.iwos.taskexecution.infrastructure.persistence.entity.TaskAssignmentEntity;
import com.iwos.taskexecution.infrastructure.persistence.repository.TaskAssignmentRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskQueryService {

    private final TaskAssignmentRepository taskRepository;
    private final TaskResponseMapper mapper;

    public TaskQueryService(TaskAssignmentRepository taskRepository, TaskResponseMapper mapper) {
        this.taskRepository = taskRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public TaskResponse getById(UUID taskAssignmentId) {
        TaskAssignmentEntity entity = taskRepository.findById(taskAssignmentId)
                .orElseThrow(() -> new TaskNotFoundException(taskAssignmentId));
        return mapper.toResponse(entity);
    }

    @Transactional(readOnly = true)
    public TaskResponse getByFulfillmentTaskId(UUID fulfillmentTaskId) {
        TaskAssignmentEntity entity = taskRepository.findByFulfillmentTaskId(fulfillmentTaskId)
                .orElseThrow(() -> new TaskNotFoundException(fulfillmentTaskId));
        return mapper.toResponse(entity);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> listTasks(TaskStatus status, String nodeId, int limit) {
        PageRequest page = PageRequest.of(0, limit);
        List<TaskAssignmentEntity> entities;

        if (nodeId != null && !nodeId.isBlank()) {
            entities = taskRepository.findByStatusAndNodeIdOrderBySourceCreatedAtAsc(status, nodeId, page);
        } else {
            entities = taskRepository.findByStatusOrderBySourceCreatedAtAsc(status, page);
        }

        return entities.stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> listByFulfillmentOrderId(UUID fulfillmentOrderId) {
        return taskRepository.findByFulfillmentOrderId(fulfillmentOrderId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}
