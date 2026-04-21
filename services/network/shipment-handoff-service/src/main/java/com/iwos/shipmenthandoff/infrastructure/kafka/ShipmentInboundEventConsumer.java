package com.iwos.shipmenthandoff.infrastructure.kafka;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwos.shipmenthandoff.application.ShipmentCommandService;
import com.iwos.shipmenthandoff.domain.shipment.CarrierCode;
import com.iwos.shipmenthandoff.domain.shipment.ShipmentAlreadyExistsException;
import com.iwos.shipmenthandoff.infrastructure.observability.ShipmentMetrics;
import java.util.Map;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumes warehouse-orchestrator events. When a pack-completed event arrives,
 * the warehouse has already verified all tasks are done — so we can create
 * the shipment directly without re-checking stale state.
 */
@Component
public class ShipmentInboundEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ShipmentInboundEventConsumer.class);
    private static final String TARGET_EVENT_TYPE = "warehouse-orchestrator.pack-completed.v1";

    private final ShipmentCommandService commandService;
    private final ShipmentMetrics metrics;
    private final ObjectMapper objectMapper;

    public ShipmentInboundEventConsumer(
            ShipmentCommandService commandService,
            ShipmentMetrics metrics,
            ObjectMapper objectMapper
    ) {
        this.commandService = commandService;
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

            if (!TARGET_EVENT_TYPE.equals(eventType)) {
                log.debug("Skipping event type: {}", eventType);
                metrics.incrementInboundEventsSkipped();
                return;
            }

            String fulfillmentOrderIdStr = (String) payload.get("fulfillmentOrderId");
            if (fulfillmentOrderIdStr == null) {
                log.warn("Missing fulfillmentOrderId in pack-completed event payload");
                metrics.incrementInboundEventsFailed();
                return;
            }

            UUID fulfillmentOrderId = UUID.fromString(fulfillmentOrderIdStr);
            log.info("Processing pack-completed event: fulfillmentOrderId={}", fulfillmentOrderId);

            // Warehouse has already confirmed all tasks completed — create shipment directly
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
