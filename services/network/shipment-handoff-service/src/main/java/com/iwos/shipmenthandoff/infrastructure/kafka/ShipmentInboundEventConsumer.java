package com.iwos.shipmenthandoff.infrastructure.kafka;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwos.shipmenthandoff.application.ShipmentCommandService;
import com.iwos.shipmenthandoff.domain.shipment.CarrierCode;
import com.iwos.shipmenthandoff.domain.shipment.ShipmentAlreadyExistsException;
import com.iwos.shipmenthandoff.infrastructure.observability.ShipmentMetrics;
import com.iwos.shipmenthandoff.infrastructure.taskexecution.FulfillmentOrderResponse;
import com.iwos.shipmenthandoff.infrastructure.taskexecution.WarehouseOrchestratorClient;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ShipmentInboundEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ShipmentInboundEventConsumer.class);
    private static final String TARGET_EVENT_TYPE = "task-execution.task-completed.v1";

    private final ShipmentCommandService commandService;
    private final WarehouseOrchestratorClient warehouseClient;
    private final ShipmentMetrics metrics;
    private final ObjectMapper objectMapper;

    public ShipmentInboundEventConsumer(
            ShipmentCommandService commandService,
            WarehouseOrchestratorClient warehouseClient,
            ShipmentMetrics metrics,
            ObjectMapper objectMapper
    ) {
        this.commandService = commandService;
        this.warehouseClient = warehouseClient;
        this.metrics = metrics;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "${shipment-handoff-service.kafka.consumer-topic}",
            groupId = "${shipment-handoff-service.kafka.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(ConsumerRecord<String, String> record) {
        try {
            log.debug("Received message: topic={}, partition={}, offset={}, key={}",
                    record.topic(), record.partition(), record.offset(), record.key());

            Map<String, Object> payload = objectMapper.readValue(
                    record.value(), new TypeReference<>() {});

            String eventType = (String) payload.get("eventType");
            
            // We're interested in PACK task completions
            if (!TARGET_EVENT_TYPE.equals(eventType)) {
                log.debug("Skipping event type: {}", eventType);
                metrics.incrementInboundEventsSkipped();
                return;
            }

            String taskType = (String) payload.get("taskType");
            if (!"PACK".equals(taskType)) {
                log.debug("Skipping non-PACK task completion");
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
            log.info("Processing PACK task completed event: fulfillmentOrderId={}", fulfillmentOrderId);

            // Check if all tasks are completed for this fulfillment order
            Optional<FulfillmentOrderResponse> optFulfillment = warehouseClient.getFulfillmentOrder(fulfillmentOrderId);
            if (optFulfillment.isEmpty()) {
                log.warn("Fulfillment order not found: {}", fulfillmentOrderId);
                metrics.incrementInboundEventsFailed();
                return;
            }

            FulfillmentOrderResponse fulfillment = optFulfillment.get();
            
            // Check if PACK task status is completed (we got the event, so it should be)
            boolean allPackCompleted = fulfillment.tasks().stream()
                    .filter(t -> "PACK".equals(t.taskType()))
                    .allMatch(t -> "COMPLETED".equals(t.taskStatus()));

            if (!allPackCompleted) {
                log.debug("Not all PACK tasks completed yet for fulfillmentOrderId={}", fulfillmentOrderId);
                metrics.incrementInboundEventsSkipped();
                return;
            }

            // Create shipment
            try {
                commandService.createShipment(fulfillmentOrderId, CarrierCode.INTERNAL, null, null);
                log.info("Auto-created shipment for fulfillmentOrderId={}", fulfillmentOrderId);
            } catch (ShipmentAlreadyExistsException e) {
                log.debug("Shipment already exists for fulfillmentOrderId={}", fulfillmentOrderId);
            }

            metrics.incrementInboundEventsProcessed();

        } catch (Exception e) {
            log.error("Failed to process message: topic={}, partition={}, offset={}",
                    record.topic(), record.partition(), record.offset(), e);
            metrics.incrementInboundEventsFailed();
        }
    }
}
