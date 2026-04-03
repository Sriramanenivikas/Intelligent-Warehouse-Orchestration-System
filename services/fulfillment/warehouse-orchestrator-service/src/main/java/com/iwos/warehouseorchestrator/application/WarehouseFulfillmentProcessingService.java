package com.iwos.warehouseorchestrator.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwos.warehouseorchestrator.domain.fulfillment.FulfillmentOrderNotFoundException;
import com.iwos.warehouseorchestrator.domain.fulfillment.FulfillmentOrderStatus;
import com.iwos.warehouseorchestrator.domain.fulfillment.FulfillmentTaskStatus;
import com.iwos.warehouseorchestrator.domain.fulfillment.FulfillmentTaskType;
import com.iwos.warehouseorchestrator.domain.fulfillment.OrderWorkflowNotReadyException;
import com.iwos.warehouseorchestrator.infrastructure.config.WarehouseOrchestratorServiceProperties;
import com.iwos.warehouseorchestrator.infrastructure.observability.WarehouseOrchestratorMetrics;
import com.iwos.warehouseorchestrator.infrastructure.orderworkflow.OrderWorkflowClient;
import com.iwos.warehouseorchestrator.infrastructure.orderworkflow.OrderWorkflowClientResponse;
import com.iwos.warehouseorchestrator.infrastructure.orderworkflow.OrderWorkflowReservationClientResponse;
import com.iwos.warehouseorchestrator.infrastructure.persistence.entity.FulfillmentOrderEntity;
import com.iwos.warehouseorchestrator.infrastructure.persistence.entity.FulfillmentTaskEntity;
import com.iwos.warehouseorchestrator.infrastructure.persistence.entity.WarehouseOutboxEventEntity;
import com.iwos.warehouseorchestrator.infrastructure.persistence.repository.FulfillmentOrderRepository;
import com.iwos.warehouseorchestrator.infrastructure.persistence.repository.WarehouseOutboxEventRepository;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WarehouseFulfillmentProcessingService {

    private static final Logger log = LoggerFactory.getLogger(WarehouseFulfillmentProcessingService.class);
    private static final String PAYMENT_AUTHORIZED = "PAYMENT_AUTHORIZED";
    private static final String RESERVATION_STATUS_RESERVED = "RESERVED";

    private final OrderWorkflowClient orderWorkflowClient;
    private final FulfillmentOrderRepository fulfillmentOrderRepository;
    private final WarehouseOutboxEventRepository warehouseOutboxEventRepository;
    private final WarehouseOrchestratorServiceProperties properties;
    private final WarehouseOrchestratorMetrics metrics;
    private final ObjectMapper objectMapper;

    public WarehouseFulfillmentProcessingService(
            OrderWorkflowClient orderWorkflowClient,
            FulfillmentOrderRepository fulfillmentOrderRepository,
            WarehouseOutboxEventRepository warehouseOutboxEventRepository,
            WarehouseOrchestratorServiceProperties properties,
            WarehouseOrchestratorMetrics metrics,
            ObjectMapper objectMapper
    ) {
        this.orderWorkflowClient = orderWorkflowClient;
        this.fulfillmentOrderRepository = fulfillmentOrderRepository;
        this.warehouseOutboxEventRepository = warehouseOutboxEventRepository;
        this.properties = properties;
        this.metrics = metrics;
        this.objectMapper = objectMapper;
    }

    /**
     * Process fulfillment for a given order — called both by HTTP manual trigger and Kafka consumer.
     * Idempotent: if fulfillment order already exists for this orderIntentId, returns existing.
     */
    @Transactional
    public UUID processFulfillment(UUID orderIntentId, String sourceTopic, String sourceMessageKey, String sourceEventPayload) {
        // Idempotency: check if fulfillment order already created
        Optional<FulfillmentOrderEntity> existing = fulfillmentOrderRepository.findByOrderIntentId(orderIntentId);
        if (existing.isPresent()) {
            log.info("Fulfillment order already exists for orderIntentId={}, skipping", orderIntentId);
            return existing.get().getFulfillmentOrderId();
        }

        // Fetch workflow from order-orchestrator
        OrderWorkflowClientResponse workflow = orderWorkflowClient.fetchWorkflow(orderIntentId);

        // Verify status
        if (!PAYMENT_AUTHORIZED.equals(workflow.status())) {
            throw new OrderWorkflowNotReadyException(orderIntentId, workflow.status());
        }

        // Extract payment info
        UUID paymentIntentId = workflow.payment() != null ? workflow.payment().paymentIntentId() : null;

        // Determine fulfillment node
        String fulfillmentNodeId = workflow.fulfillmentNodeId() != null
                ? workflow.fulfillmentNodeId()
                : properties.getDefaultFulfillmentNodeId();

        // Create fulfillment order
        UUID fulfillmentOrderId = UUID.randomUUID();
        FulfillmentOrderEntity order = new FulfillmentOrderEntity();
        order.setFulfillmentOrderId(fulfillmentOrderId);
        order.setWorkflowId(workflow.workflowId());
        order.setOrderIntentId(orderIntentId);
        order.setPaymentIntentId(paymentIntentId);
        order.setCustomerId(workflow.customerId());
        order.setFulfillmentNodeId(fulfillmentNodeId);
        order.setWarehouseCode(properties.getWarehouseCode());
        order.setSourceTopic(sourceTopic != null ? sourceTopic : "manual");
        order.setSourceMessageKey(sourceMessageKey != null ? sourceMessageKey : "manual-" + orderIntentId);
        order.setSourceEventType("order-orchestrator.payment-authorized.v1");
        order.setSourceEventOccurredAt(workflow.updatedAt() != null ? workflow.updatedAt() : Instant.now());
        order.setSourceEventPayload(sourceEventPayload != null ? sourceEventPayload : "{}");
        order.setStatus(FulfillmentOrderStatus.TASKS_CREATED);

        // Create PICK tasks from RESERVED reservations
        List<OrderWorkflowReservationClientResponse> reservedItems = workflow.reservations() != null
                ? workflow.reservations().stream()
                    .filter(r -> RESERVATION_STATUS_RESERVED.equals(r.status()))
                    .toList()
                : List.of();

        int seq = 1;
        for (OrderWorkflowReservationClientResponse reservation : reservedItems) {
            FulfillmentTaskEntity pickTask = new FulfillmentTaskEntity();
            pickTask.setFulfillmentTaskId(UUID.randomUUID());
            pickTask.setTaskType(FulfillmentTaskType.PICK);
            pickTask.setStatus(FulfillmentTaskStatus.READY);
            pickTask.setSequenceNumber(seq++);
            pickTask.setNodeId(reservation.nodeId() != null ? reservation.nodeId() : fulfillmentNodeId);
            pickTask.setTaskTitle("PICK %s x%d".formatted(reservation.sku(), reservation.quantity()));
            pickTask.setTaskPayload(serializeTaskPayload(Map.of(
                    "orderIntentItemId", String.valueOf(reservation.orderIntentItemId()),
                    "inventoryReservationId", String.valueOf(reservation.inventoryReservationId()),
                    "sku", reservation.sku(),
                    "quantity", reservation.quantity(),
                    "nodeId", reservation.nodeId() != null ? reservation.nodeId() : fulfillmentNodeId
            )));
            order.addTask(pickTask);
        }

        // Create one PACK task with status=BLOCKED (waits for all PICKs to complete)
        FulfillmentTaskEntity packTask = new FulfillmentTaskEntity();
        packTask.setFulfillmentTaskId(UUID.randomUUID());
        packTask.setTaskType(FulfillmentTaskType.PACK);
        packTask.setStatus(FulfillmentTaskStatus.BLOCKED);
        packTask.setSequenceNumber(seq);
        packTask.setNodeId(fulfillmentNodeId);
        packTask.setTaskTitle("PACK order %s".formatted(orderIntentId));
        packTask.setTaskPayload(serializeTaskPayload(Map.of(
                "orderIntentId", orderIntentId.toString(),
                "fulfillmentNodeId", fulfillmentNodeId,
                "totalPickTasks", reservedItems.size()
        )));
        order.addTask(packTask);

        // Persist fulfillment order with tasks
        fulfillmentOrderRepository.save(order);

        // Create outbox event
        WarehouseOutboxEventEntity outboxEvent = new WarehouseOutboxEventEntity();
        outboxEvent.setOutboxEventId(UUID.randomUUID());
        outboxEvent.setAggregateType("FulfillmentOrder");
        outboxEvent.setAggregateId(fulfillmentOrderId);
        outboxEvent.setEventType("warehouse-orchestrator.fulfillment-created.v1");
        outboxEvent.setStatus("PENDING");
        outboxEvent.setAttempts(0);
        outboxEvent.setPayload(serializeOutboxPayload(order, reservedItems.size()));
        warehouseOutboxEventRepository.save(outboxEvent);

        int totalTasks = reservedItems.size() + 1; // picks + 1 pack
        metrics.recordFulfillmentOrderCreated();
        metrics.recordFulfillmentTasksCreated(totalTasks);

        log.info("Created fulfillment order={} with {} tasks ({}P +1K) for orderIntentId={}",
                fulfillmentOrderId, totalTasks, reservedItems.size(), orderIntentId);

        return fulfillmentOrderId;
    }

    private String serializeTaskPayload(Map<String, Object> data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            log.warn("Failed to serialize task payload, using empty JSON", e);
            return "{}";
        }
    }

    private String serializeOutboxPayload(FulfillmentOrderEntity order, int pickTaskCount) {
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
            payload.put("pickTaskCount", pickTaskCount);
            payload.put("totalTaskCount", pickTaskCount + 1);
            payload.put("eventType", "warehouse-orchestrator.fulfillment-created.v1");
            payload.put("eventOccurredAt", Instant.now().toString());
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            log.warn("Failed to serialize outbox payload, using minimal JSON", e);
            return "{\"fulfillmentOrderId\":\"%s\"}".formatted(order.getFulfillmentOrderId());
        }
    }
}
