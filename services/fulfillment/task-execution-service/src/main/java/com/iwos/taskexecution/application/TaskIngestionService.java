package com.iwos.taskexecution.application;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwos.taskexecution.domain.task.TaskStatus;
import com.iwos.taskexecution.domain.task.TaskType;
import com.iwos.taskexecution.infrastructure.observability.TaskExecutionMetrics;
import com.iwos.taskexecution.infrastructure.persistence.entity.TaskAssignmentEntity;
import com.iwos.taskexecution.infrastructure.persistence.repository.TaskAssignmentRepository;
import com.iwos.taskexecution.infrastructure.warehouse.WarehouseFulfillmentOrderResponse;
import com.iwos.taskexecution.infrastructure.warehouse.WarehouseOrchestratorClient;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskIngestionService {

    private static final Logger log = LoggerFactory.getLogger(TaskIngestionService.class);

    private final TaskAssignmentRepository taskRepository;
    private final WarehouseOrchestratorClient warehouseClient;
    private final TaskExecutionMetrics metrics;
    private final ObjectMapper objectMapper;

    public TaskIngestionService(
            TaskAssignmentRepository taskRepository,
            WarehouseOrchestratorClient warehouseClient,
            TaskExecutionMetrics metrics,
            ObjectMapper objectMapper
    ) {
        this.taskRepository = taskRepository;
        this.warehouseClient = warehouseClient;
        this.metrics = metrics;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public int ingestTasksForFulfillmentOrder(UUID fulfillmentOrderId) {
        Optional<WarehouseFulfillmentOrderResponse> optFulfillment = warehouseClient.getFulfillmentOrder(fulfillmentOrderId);
        if (optFulfillment.isEmpty()) {
            log.warn("Fulfillment order not found: {}", fulfillmentOrderId);
            return 0;
        }

        WarehouseFulfillmentOrderResponse fulfillment = optFulfillment.get();
        int ingested = 0;

        for (WarehouseFulfillmentOrderResponse.TaskItem task : fulfillment.tasks()) {
            if (taskRepository.existsByFulfillmentTaskId(task.fulfillmentTaskId())) {
                log.debug("Task already ingested: {}", task.fulfillmentTaskId());
                continue;
            }

            TaskAssignmentEntity entity = new TaskAssignmentEntity();
            entity.setTaskAssignmentId(UUID.randomUUID());
            entity.setFulfillmentTaskId(task.fulfillmentTaskId());
            entity.setFulfillmentOrderId(fulfillment.fulfillmentOrderId());
            entity.setOrderIntentId(fulfillment.orderIntentId());
            entity.setTaskType(TaskType.valueOf(task.taskType()));

            String sourceStatus = task.taskStatus();
            entity.setStatus(TaskStatus.valueOf(sourceStatus));

            entity.setNodeId(task.nodeId());
            entity.setTaskTitle(task.taskTitle());

            try {
                entity.setTaskPayload(objectMapper.writeValueAsString(task.taskPayload()));
            } catch (Exception e) {
                log.warn("Failed to serialize task payload for task {}", task.fulfillmentTaskId(), e);
            }

            Instant now = Instant.now();
            entity.setSourceCreatedAt(task.createdAt());
            entity.setCreatedAt(now);
            entity.setUpdatedAt(now);

            taskRepository.save(entity);
            ingested++;
            metrics.incrementTasksIngested();

            log.info("Ingested task: fulfillmentTaskId={}, type={}, status={}",
                    task.fulfillmentTaskId(), task.taskType(), entity.getStatus());
        }

        return ingested;
    }

    @Transactional
    public int ingestTasksForOrderIntent(UUID orderIntentId) {
        Optional<WarehouseFulfillmentOrderResponse> optFulfillment =
                warehouseClient.getFulfillmentOrderByOrderIntentId(orderIntentId);
        if (optFulfillment.isEmpty()) {
            log.warn("Fulfillment order not found for orderIntentId: {}", orderIntentId);
            return 0;
        }

        return ingestTasksForFulfillmentOrder(optFulfillment.get().fulfillmentOrderId());
    }
}
