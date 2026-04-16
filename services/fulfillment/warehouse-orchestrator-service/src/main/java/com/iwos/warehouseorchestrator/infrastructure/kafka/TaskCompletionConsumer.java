package com.iwos.warehouseorchestrator.infrastructure.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwos.warehouseorchestrator.application.TaskStateUpdateService;
import com.iwos.warehouseorchestrator.infrastructure.observability.WarehouseOrchestratorMetrics;
import java.util.Set;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * Consumes task lifecycle events from task-execution-service and updates
 * warehouse fulfillment task state accordingly.
 *
 * This is the critical bridge that closes the state propagation gap between
 * task-execution (which runs work) and warehouse-orchestrator (source of truth for task state).
 */
@Component
public class TaskCompletionConsumer {

    private static final Logger log = LoggerFactory.getLogger(TaskCompletionConsumer.class);

    private static final Set<String> TARGET_EVENT_TYPES = Set.of(
            "task-execution.task-completed.v1",
            "task-execution.task-failed.v1"
    );

    private final TaskStateUpdateService taskStateUpdateService;
    private final WarehouseOrchestratorMetrics metrics;
    private final ObjectMapper objectMapper;

    public TaskCompletionConsumer(
            TaskStateUpdateService taskStateUpdateService,
            WarehouseOrchestratorMetrics metrics,
            ObjectMapper objectMapper
    ) {
        this.taskStateUpdateService = taskStateUpdateService;
        this.metrics = metrics;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "${iwos.warehouse-orchestrator.kafka.task-execution-inbound-topic}",
            groupId = "warehouse-orchestrator-task-completion",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onMessage(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        String key = record.key();
        String value = record.value();

        log.debug("Received task-execution event: topic={}, key={}, partition={}, offset={}",
                record.topic(), key, record.partition(), record.offset());

        try {
            JsonNode payload = objectMapper.readTree(value);
            String eventType = payload.has("eventType") ? payload.get("eventType").asText() : null;

            if (eventType == null || !TARGET_EVENT_TYPES.contains(eventType)) {
                log.debug("Skipping non-target event type: {}", eventType);
                acknowledgment.acknowledge();
                return;
            }

            metrics.recordTaskStateUpdateReceived();

            // Extract required fields
            String fulfillmentTaskIdStr = getStringField(payload, "fulfillmentTaskId");
            String fulfillmentOrderIdStr = getStringField(payload, "fulfillmentOrderId");
            String taskType = getStringField(payload, "taskType");

            if (fulfillmentTaskIdStr == null || fulfillmentOrderIdStr == null) {
                log.warn("Missing required fields in task-execution event: fulfillmentTaskId={}, fulfillmentOrderId={}",
                        fulfillmentTaskIdStr, fulfillmentOrderIdStr);
                metrics.recordTaskStateUpdateFailed();
                acknowledgment.acknowledge();
                return;
            }

            UUID fulfillmentTaskId = UUID.fromString(fulfillmentTaskIdStr);
            UUID fulfillmentOrderId = UUID.fromString(fulfillmentOrderIdStr);

            if ("task-execution.task-completed.v1".equals(eventType)) {
                log.info("Processing task-completed event: fulfillmentTaskId={}, taskType={}", fulfillmentTaskId, taskType);
                taskStateUpdateService.handleTaskCompleted(fulfillmentTaskId, fulfillmentOrderId, taskType);
            } else if ("task-execution.task-failed.v1".equals(eventType)) {
                String reason = getStringField(payload, "failureReason");
                log.info("Processing task-failed event: fulfillmentTaskId={}, taskType={}, reason={}",
                        fulfillmentTaskId, taskType, reason);
                taskStateUpdateService.handleTaskFailed(fulfillmentTaskId, fulfillmentOrderId, taskType, reason);
            }

        } catch (Exception e) {
            log.error("Failed to process task-execution event: key={}, error={}", key, e.getMessage(), e);
            metrics.recordTaskStateUpdateFailed();
        }

        acknowledgment.acknowledge();
    }

    private String getStringField(JsonNode payload, String field) {
        return payload.has(field) && !payload.get(field).isNull() ? payload.get(field).asText() : null;
    }
}
