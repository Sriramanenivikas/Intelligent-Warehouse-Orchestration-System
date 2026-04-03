package com.iwos.orderorchestrator.infrastructure.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwos.orderorchestrator.application.OrderWorkflowProcessingService;
import com.iwos.orderorchestrator.infrastructure.observability.OrderWorkflowMetrics;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * Consumes order-intake accepted events from Kafka and triggers workflow processing.
 */
@Component
public class OrderIntakeEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderIntakeEventConsumer.class);

    private final OrderWorkflowProcessingService orderWorkflowProcessingService;
    private final ObjectMapper objectMapper;
    private final OrderWorkflowMetrics orderWorkflowMetrics;

    public OrderIntakeEventConsumer(
            OrderWorkflowProcessingService orderWorkflowProcessingService,
            ObjectMapper objectMapper,
            OrderWorkflowMetrics orderWorkflowMetrics
    ) {
        this.orderWorkflowProcessingService = orderWorkflowProcessingService;
        this.objectMapper = objectMapper;
        this.orderWorkflowMetrics = orderWorkflowMetrics;
    }

    @KafkaListener(
            topics = "${iwos.order-orchestrator.kafka.order-intake-topic:iwos.order-intake.accepted.v1}",
            groupId = "${spring.kafka.consumer.group-id:order-orchestrator-service}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeOrderIntakeEvent(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        String key = record.key();
        String value = record.value();

        log.info("Received order-intake event: key={}, partition={}, offset={}",
                key, record.partition(), record.offset());

        try {
            JsonNode eventNode = objectMapper.readTree(value);
            UUID orderIntentId = UUID.fromString(eventNode.get("aggregateId").asText());

            log.info("Processing order intent: {}", orderIntentId);
            
            var result = orderWorkflowProcessingService.processOrderIntent(orderIntentId);
            
            log.info("Workflow completed for orderIntentId={}, workflowId={}, status={}",
                    orderIntentId, result.workflowId(), result.status());

            orderWorkflowMetrics.recordKafkaConsume("processed");
            acknowledgment.acknowledge();

        } catch (JsonProcessingException exception) {
            log.error("Failed to parse order-intake event: {}", value, exception);
            orderWorkflowMetrics.recordKafkaConsume("parse_failed");
            // Acknowledge to avoid infinite retry on bad message
            acknowledgment.acknowledge();
        } catch (Exception exception) {
            log.error("Failed to process order-intake event for key={}", key, exception);
            orderWorkflowMetrics.recordKafkaConsume("failed");
            // Don't acknowledge - will be retried
            throw exception;
        }
    }
}
