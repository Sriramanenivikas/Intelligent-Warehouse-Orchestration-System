package com.iwos.warehouseorchestrator.infrastructure.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwos.warehouseorchestrator.application.WarehouseFulfillmentProcessingService;
import com.iwos.warehouseorchestrator.infrastructure.config.WarehouseOrchestratorServiceProperties;
import com.iwos.warehouseorchestrator.infrastructure.observability.WarehouseOrchestratorMetrics;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class WarehouseInboundEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(WarehouseInboundEventConsumer.class);
    private static final String TARGET_EVENT_TYPE = "order-orchestrator.payment-authorized.v1";

    private final WarehouseFulfillmentProcessingService processingService;
    private final WarehouseOrchestratorMetrics metrics;
    private final ObjectMapper objectMapper;

    public WarehouseInboundEventConsumer(
            WarehouseFulfillmentProcessingService processingService,
            WarehouseOrchestratorServiceProperties properties,
            WarehouseOrchestratorMetrics metrics,
            ObjectMapper objectMapper
    ) {
        this.processingService = processingService;
        this.metrics = metrics;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "${iwos.warehouse-orchestrator.kafka.inbound-topic}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onMessage(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        String key = record.key();
        String value = record.value();
        String topic = record.topic();

        log.info("Received Kafka message: topic={}, key={}, partition={}, offset={}",
                topic, key, record.partition(), record.offset());

        try {
            JsonNode payload = objectMapper.readTree(value);
            String eventType = payload.has("eventType") ? payload.get("eventType").asText() : null;

            if (!TARGET_EVENT_TYPE.equals(eventType)) {
                log.debug("Skipping event with eventType={} (expected {})", eventType, TARGET_EVENT_TYPE);
                metrics.recordInboundSkipped();
                acknowledgment.acknowledge();
                return;
            }

            // Extract orderIntentId from the event payload
            String orderIntentIdStr = extractOrderIntentId(payload);
            if (orderIntentIdStr == null) {
                log.warn("Could not extract orderIntentId from payment-authorized event, key={}", key);
                metrics.recordInboundFailed();
                acknowledgment.acknowledge();
                return;
            }

            UUID orderIntentId = UUID.fromString(orderIntentIdStr);

            log.info("Processing payment-authorized event for orderIntentId={}", orderIntentId);
            processingService.processFulfillment(orderIntentId, topic, key, value);
            metrics.recordInboundProcessed();

        } catch (Exception e) {
            log.error("Failed to process inbound event: key={}, error={}", key, e.getMessage(), e);
            metrics.recordInboundFailed();
        }

        acknowledgment.acknowledge();
    }

    private String extractOrderIntentId(JsonNode payload) {
        // Try common event envelope patterns
        if (payload.has("orderIntentId")) {
            return payload.get("orderIntentId").asText();
        }
        if (payload.has("data") && payload.get("data").has("orderIntentId")) {
            return payload.get("data").get("orderIntentId").asText();
        }
        if (payload.has("aggregateId")) {
            return payload.get("aggregateId").asText();
        }
        return null;
    }
}
