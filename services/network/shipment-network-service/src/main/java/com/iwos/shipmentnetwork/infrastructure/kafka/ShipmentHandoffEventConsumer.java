package com.iwos.shipmentnetwork.infrastructure.kafka;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwos.shipmentnetwork.application.ShipmentNetworkCommandService;
import com.iwos.shipmentnetwork.infrastructure.observability.ShipmentNetworkMetrics;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class ShipmentHandoffEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ShipmentHandoffEventConsumer.class);
    private static final String TARGET_EVENT_TYPE = "shipment-handoff.shipment-created.v1";

    private final ShipmentNetworkCommandService commandService;
    private final ShipmentNetworkMetrics metrics;
    private final ObjectMapper objectMapper;

    public ShipmentHandoffEventConsumer(
            ShipmentNetworkCommandService commandService,
            ShipmentNetworkMetrics metrics,
            ObjectMapper objectMapper
    ) {
        this.commandService = commandService;
        this.metrics = metrics;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "${shipment-network-service.kafka.consumer-topic}",
            groupId = "${shipment-network-service.kafka.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        try {
            Map<String, Object> payload = objectMapper.readValue(record.value(), new TypeReference<>() {});
            String eventType = (String) payload.get("eventType");
            if (!TARGET_EVENT_TYPE.equals(eventType)) {
                metrics.incrementInboundEventsSkipped();
                acknowledgment.acknowledge();
                return;
            }

            commandService.createFromShipmentCreatedEvent(payload);
            metrics.incrementInboundEventsProcessed();
            log.info("Created network shipment from handoff event: key={}", record.key());
        } catch (Exception e) {
            metrics.incrementInboundEventsFailed();
            log.error("Failed to consume shipment handoff event: topic={}, partition={}, offset={}",
                    record.topic(), record.partition(), record.offset(), e);
        }
        acknowledgment.acknowledge();
    }
}
