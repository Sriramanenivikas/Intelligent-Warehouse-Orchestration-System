package com.iwos.taskexecution.infrastructure.kafka;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwos.taskexecution.application.TaskIngestionService;
import com.iwos.taskexecution.infrastructure.observability.TaskExecutionMetrics;
import java.util.Map;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TaskInboundEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(TaskInboundEventConsumer.class);
    private static final String TARGET_EVENT_TYPE = "warehouse-orchestrator.fulfillment-created.v1";

    private final TaskIngestionService ingestionService;
    private final TaskExecutionMetrics metrics;
    private final ObjectMapper objectMapper;

    public TaskInboundEventConsumer(
            TaskIngestionService ingestionService,
            TaskExecutionMetrics metrics,
            ObjectMapper objectMapper
    ) {
        this.ingestionService = ingestionService;
        this.metrics = metrics;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "${task-execution-service.kafka.consumer-topic}",
            groupId = "${task-execution-service.kafka.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(ConsumerRecord<String, String> record) {
        try {
            log.debug("Received message: topic={}, partition={}, offset={}, key={}",
                    record.topic(), record.partition(), record.offset(), record.key());

            Map<String, Object> payload = objectMapper.readValue(
                    record.value(), new TypeReference<>() {});

            String eventType = (String) payload.get("eventType");
            if (!TARGET_EVENT_TYPE.equals(eventType)) {
                log.debug("Skipping event type: {}", eventType);
                metrics.incrementInboundEventsSkipped();
                return;
            }

            String fulfillmentOrderIdStr = (String) payload.get("fulfillmentOrderId");
            if (fulfillmentOrderIdStr == null) {
                log.warn("Missing fulfillmentOrderId in event payload");
                metrics.incrementInboundEventsFailed();
                return;
            }

            UUID fulfillmentOrderId = UUID.fromString(fulfillmentOrderIdStr);
            log.info("Processing fulfillment-created event: fulfillmentOrderId={}", fulfillmentOrderId);

            int ingested = ingestionService.ingestTasksForFulfillmentOrder(fulfillmentOrderId);
            log.info("Ingested {} tasks for fulfillmentOrderId={}", ingested, fulfillmentOrderId);

            metrics.incrementInboundEventsProcessed();

        } catch (Exception e) {
            log.error("Failed to process message: topic={}, partition={}, offset={}",
                    record.topic(), record.partition(), record.offset(), e);
            metrics.incrementInboundEventsFailed();
        }
    }
}
