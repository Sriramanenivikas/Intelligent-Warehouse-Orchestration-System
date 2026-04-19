package com.iwos.notification.infrastructure.kafka;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwos.notification.application.NotificationCommandService;
import com.iwos.notification.infrastructure.observability.NotificationMetrics;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class ScanEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ScanEventConsumer.class);

    private final NotificationCommandService commandService;
    private final NotificationMetrics metrics;
    private final ObjectMapper objectMapper;

    public ScanEventConsumer(
            NotificationCommandService commandService,
            NotificationMetrics metrics,
            ObjectMapper objectMapper
    ) {
        this.commandService = commandService;
        this.metrics = metrics;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "${notification-service.kafka.consumer-topic}",
            groupId = "${notification-service.kafka.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onMessage(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        try {
            log.debug("Received message: topic={}, partition={}, offset={}, key={}",
                    record.topic(), record.partition(), record.offset(), record.key());
            Map<String, Object> payload = objectMapper.readValue(record.value(), new TypeReference<>() {});
            commandService.consumeScanMilestoneEvent(payload);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            metrics.incrementInboundEventsFailed();
            log.error("Failed to process notification event: topic={}, offset={}", record.topic(), record.offset(), e);
            acknowledgment.acknowledge();
        }
    }
}
