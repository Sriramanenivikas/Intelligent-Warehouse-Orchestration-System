package com.iwos.scanevent.infrastructure.kafka;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwos.scanevent.application.ScanEventCommandService;
import com.iwos.scanevent.infrastructure.config.ScanEventServiceProperties;
import com.iwos.scanevent.infrastructure.observability.ScanEventMetrics;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class ShipmentNetworkEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ShipmentNetworkEventConsumer.class);

    private final ScanEventCommandService commandService;
    private final ScanEventMetrics metrics;
    private final ObjectMapper objectMapper;
    private final String targetTopic;

    public ShipmentNetworkEventConsumer(
            ScanEventCommandService commandService,
            ScanEventMetrics metrics,
            ObjectMapper objectMapper,
            ScanEventServiceProperties properties
    ) {
        this.commandService = commandService;
        this.metrics = metrics;
        this.objectMapper = objectMapper;
        this.targetTopic = properties.kafka().consumerTopic();
    }

    @KafkaListener(
            topics = "${scan-event-service.kafka.consumer-topic}",
            groupId = "${scan-event-service.kafka.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onMessage(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        try {
            log.debug("Received message: topic={}, partition={}, offset={}, key={}",
                    record.topic(), record.partition(), record.offset(), record.key());
            if (!targetTopic.equals(record.topic())) {
                metrics.incrementInboundEventsSkipped();
                acknowledgment.acknowledge();
                return;
            }

            Map<String, Object> payload = objectMapper.readValue(record.value(), new TypeReference<>() {});
            commandService.consumeShipmentNetworkEvent(payload);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            metrics.incrementInboundEventsFailed();
            log.error("Failed to process shipment-network event: topic={}, offset={}", record.topic(), record.offset(), e);
            acknowledgment.acknowledge();
        }
    }
}
