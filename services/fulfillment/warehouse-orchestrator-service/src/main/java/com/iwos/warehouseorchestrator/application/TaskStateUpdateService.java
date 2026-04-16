package com.iwos.warehouseorchestrator.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwos.warehouseorchestrator.domain.fulfillment.FulfillmentOrderStatus;
import com.iwos.warehouseorchestrator.domain.fulfillment.FulfillmentTaskStatus;
import com.iwos.warehouseorchestrator.domain.fulfillment.FulfillmentTaskType;
import com.iwos.warehouseorchestrator.infrastructure.observability.WarehouseOrchestratorMetrics;
import com.iwos.warehouseorchestrator.infrastructure.persistence.entity.FulfillmentOrderEntity;
import com.iwos.warehouseorchestrator.infrastructure.persistence.entity.FulfillmentTaskEntity;
import com.iwos.warehouseorchestrator.infrastructure.persistence.entity.WarehouseOutboxEventEntity;
import com.iwos.warehouseorchestrator.infrastructure.persistence.repository.FulfillmentOrderRepository;
import com.iwos.warehouseorchestrator.infrastructure.persistence.repository.FulfillmentTaskRepository;
import com.iwos.warehouseorchestrator.infrastructure.persistence.repository.WarehouseOutboxEventRepository;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles task lifecycle state updates received from task-execution-service via Kafka.
 * Warehouse-orchestrator is the source of truth for fulfillment task state.
 */
@Service
public class TaskStateUpdateService {

    private static final Logger log = LoggerFactory.getLogger(TaskStateUpdateService.class);

    private final FulfillmentTaskRepository taskRepository;
    private final FulfillmentOrderRepository orderRepository;
    private final WarehouseOutboxEventRepository outboxRepository;
    private final WarehouseOrchestratorMetrics metrics;
    private final ObjectMapper objectMapper;

    public TaskStateUpdateService(
            FulfillmentTaskRepository taskRepository,
            FulfillmentOrderRepository orderRepository,
            WarehouseOutboxEventRepository outboxRepository,
            WarehouseOrchestratorMetrics metrics,
            ObjectMapper objectMapper
    ) {
        this.taskRepository = taskRepository;
        this.orderRepository = orderRepository;
        this.outboxRepository = outboxRepository;
        this.metrics = metrics;
        this.objectMapper = objectMapper;
    }

    /**
     * Update a fulfillment task's status based on an event from task-execution.
     * Then check if all tasks of the parent order are complete and emit events accordingly.
     */
    @Transactional
    public void handleTaskCompleted(UUID fulfillmentTaskId, UUID fulfillmentOrderId, String taskType) {
        updateTaskStatus(fulfillmentTaskId, FulfillmentTaskStatus.COMPLETED, fulfillmentOrderId, taskType);
    }

    @Transactional
    public void handleTaskFailed(UUID fulfillmentTaskId, UUID fulfillmentOrderId, String taskType, String reason) {
        updateTaskStatus(fulfillmentTaskId, FulfillmentTaskStatus.FAILED, fulfillmentOrderId, taskType);
    }

    private void updateTaskStatus(UUID fulfillmentTaskId, FulfillmentTaskStatus newStatus,
                                   UUID fulfillmentOrderId, String taskType) {
        Optional<FulfillmentTaskEntity> optTask = taskRepository.findByFulfillmentTaskId(fulfillmentTaskId);
        if (optTask.isEmpty()) {
            log.warn("Task not found for state update: fulfillmentTaskId={}", fulfillmentTaskId);
            metrics.recordTaskStateUpdateFailed();
            return;
        }

        FulfillmentTaskEntity task = optTask.get();

        // Idempotency: skip if already in the target state
        if (task.getStatus() == newStatus) {
            log.debug("Task already in status {}: fulfillmentTaskId={}", newStatus, fulfillmentTaskId);
            return;
        }

        FulfillmentTaskStatus oldStatus = task.getStatus();
        task.setStatus(newStatus);
        taskRepository.save(task);
        metrics.recordTaskStateUpdateApplied();

        log.info("Updated task state: fulfillmentTaskId={}, {} -> {}, type={}",
                fulfillmentTaskId, oldStatus, newStatus, taskType);

        // Check order-level state transitions
        checkOrderStateTransitions(fulfillmentOrderId);
    }

    /**
     * Check if fulfillment order should transition to a new state based on task completion.
     * State machine:
     *   TASKS_CREATED -> PICKING_IN_PROGRESS (when first PICK completes)
     *   PICKING_IN_PROGRESS -> PACKING_IN_PROGRESS (when all PICKs complete and PACK starts)
     *   PACKING_IN_PROGRESS -> PACKED (when PACK completes)
     *   PACKED -> emits pack-completed event for shipment-handoff
     */
    private void checkOrderStateTransitions(UUID fulfillmentOrderId) {
        Optional<FulfillmentOrderEntity> optOrder = orderRepository.findDetailedByFulfillmentOrderId(fulfillmentOrderId);
        if (optOrder.isEmpty()) {
            log.warn("Fulfillment order not found for state transition check: {}", fulfillmentOrderId);
            return;
        }

        FulfillmentOrderEntity order = optOrder.get();

        // Count completed tasks by type
        long completedPickCount = taskRepository.countByFulfillmentOrder_FulfillmentOrderIdAndTaskTypeAndStatus(
                fulfillmentOrderId, FulfillmentTaskType.PICK, FulfillmentTaskStatus.COMPLETED);
        long totalPickCount = taskRepository.countByFulfillmentOrder_FulfillmentOrderIdAndTaskTypeAndStatus(
                fulfillmentOrderId, FulfillmentTaskType.PICK, FulfillmentTaskStatus.COMPLETED)
                + taskRepository.countByFulfillmentOrder_FulfillmentOrderIdAndTaskTypeAndStatus(
                fulfillmentOrderId, FulfillmentTaskType.PICK, FulfillmentTaskStatus.READY)
                + taskRepository.countByFulfillmentOrder_FulfillmentOrderIdAndTaskTypeAndStatus(
                fulfillmentOrderId, FulfillmentTaskType.PICK, FulfillmentTaskStatus.BLOCKED)
                + taskRepository.countByFulfillmentOrder_FulfillmentOrderIdAndTaskTypeAndStatus(
                fulfillmentOrderId, FulfillmentTaskType.PICK, FulfillmentTaskStatus.IN_PROGRESS)
                + taskRepository.countByFulfillmentOrder_FulfillmentOrderIdAndTaskTypeAndStatus(
                fulfillmentOrderId, FulfillmentTaskType.PICK, FulfillmentTaskStatus.FAILED);
        long completedPackCount = taskRepository.countByFulfillmentOrder_FulfillmentOrderIdAndTaskTypeAndStatus(
                fulfillmentOrderId, FulfillmentTaskType.PACK, FulfillmentTaskStatus.COMPLETED);
        long totalPackCount = taskRepository.countByFulfillmentOrder_FulfillmentOrderIdAndTaskTypeAndStatus(
                fulfillmentOrderId, FulfillmentTaskType.PACK, FulfillmentTaskStatus.COMPLETED)
                + taskRepository.countByFulfillmentOrder_FulfillmentOrderIdAndTaskTypeAndStatus(
                fulfillmentOrderId, FulfillmentTaskType.PACK, FulfillmentTaskStatus.READY)
                + taskRepository.countByFulfillmentOrder_FulfillmentOrderIdAndTaskTypeAndStatus(
                fulfillmentOrderId, FulfillmentTaskType.PACK, FulfillmentTaskStatus.BLOCKED)
                + taskRepository.countByFulfillmentOrder_FulfillmentOrderIdAndTaskTypeAndStatus(
                fulfillmentOrderId, FulfillmentTaskType.PACK, FulfillmentTaskStatus.IN_PROGRESS)
                + taskRepository.countByFulfillmentOrder_FulfillmentOrderIdAndTaskTypeAndStatus(
                fulfillmentOrderId, FulfillmentTaskType.PACK, FulfillmentTaskStatus.FAILED);

        boolean allPicksCompleted = totalPickCount > 0 && completedPickCount == totalPickCount;
        boolean allPacksCompleted = totalPackCount > 0 && completedPackCount == totalPackCount;

        FulfillmentOrderStatus currentStatus = order.getStatus();

        // Transition: TASKS_CREATED -> PICKING_IN_PROGRESS (first PICK completes)
        if (currentStatus == FulfillmentOrderStatus.TASKS_CREATED && completedPickCount > 0) {
            order.setStatus(FulfillmentOrderStatus.PICKING_IN_PROGRESS);
            orderRepository.save(order);
            log.info("Fulfillment order transitioned to PICKING_IN_PROGRESS: {}", fulfillmentOrderId);
            currentStatus = FulfillmentOrderStatus.PICKING_IN_PROGRESS;
        }

        // Transition: PICKING_IN_PROGRESS -> PACKING_IN_PROGRESS (all PICKs done)
        if ((currentStatus == FulfillmentOrderStatus.PICKING_IN_PROGRESS
                || currentStatus == FulfillmentOrderStatus.TASKS_CREATED) && allPicksCompleted) {
            order.setStatus(FulfillmentOrderStatus.PACKING_IN_PROGRESS);
            orderRepository.save(order);
            log.info("Fulfillment order transitioned to PACKING_IN_PROGRESS: {}", fulfillmentOrderId);
            currentStatus = FulfillmentOrderStatus.PACKING_IN_PROGRESS;

            // Unblock PACK tasks in warehouse state
            unblockPackTasks(fulfillmentOrderId);
        }

        // Transition: PACKING_IN_PROGRESS -> PACKED (all PACKs done)
        if (currentStatus == FulfillmentOrderStatus.PACKING_IN_PROGRESS && allPacksCompleted) {
            order.setStatus(FulfillmentOrderStatus.PACKED);
            orderRepository.save(order);
            log.info("Fulfillment order transitioned to PACKED: {}", fulfillmentOrderId);

            // Emit pack-completed event for shipment-handoff
            emitPackCompletedEvent(order);
        }
    }

    private void unblockPackTasks(UUID fulfillmentOrderId) {
        var tasks = taskRepository.findByFulfillmentOrder_FulfillmentOrderId(fulfillmentOrderId);
        for (FulfillmentTaskEntity task : tasks) {
            if (task.getTaskType() == FulfillmentTaskType.PACK && task.getStatus() == FulfillmentTaskStatus.BLOCKED) {
                task.setStatus(FulfillmentTaskStatus.READY);
                taskRepository.save(task);
                log.info("Unblocked PACK task: fulfillmentTaskId={}", task.getFulfillmentTaskId());
            }
        }
    }

    private void emitPackCompletedEvent(FulfillmentOrderEntity order) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("fulfillmentOrderId", order.getFulfillmentOrderId().toString());
            payload.put("workflowId", order.getWorkflowId().toString());
            payload.put("orderIntentId", order.getOrderIntentId().toString());
            payload.put("paymentIntentId", order.getPaymentIntentId() != null ? order.getPaymentIntentId().toString() : null);
            payload.put("customerId", order.getCustomerId());
            payload.put("fulfillmentNodeId", order.getFulfillmentNodeId());
            payload.put("warehouseCode", order.getWarehouseCode());
            payload.put("status", order.getStatus().name());
            payload.put("eventType", "warehouse-orchestrator.pack-completed.v1");
            payload.put("eventOccurredAt", Instant.now().toString());

            WarehouseOutboxEventEntity outbox = new WarehouseOutboxEventEntity();
            outbox.setOutboxEventId(UUID.randomUUID());
            outbox.setAggregateType("FulfillmentOrder");
            outbox.setAggregateId(order.getFulfillmentOrderId());
            outbox.setEventType("warehouse-orchestrator.pack-completed.v1");
            outbox.setStatus("PENDING");
            outbox.setAttempts(0);
            outbox.setPayload(objectMapper.writeValueAsString(payload));
            outboxRepository.save(outbox);

            log.info("Emitted pack-completed event: fulfillmentOrderId={}", order.getFulfillmentOrderId());
        } catch (Exception e) {
            log.error("Failed to emit pack-completed event: {}", order.getFulfillmentOrderId(), e);
        }
    }
}
