package com.iwos.warehouseorchestrator.infrastructure.kafka;

import com.iwos.warehouseorchestrator.infrastructure.config.WarehouseOrchestratorServiceProperties;
import com.iwos.warehouseorchestrator.infrastructure.observability.WarehouseOrchestratorMetrics;
import com.iwos.warehouseorchestrator.infrastructure.persistence.entity.WarehouseOutboxEventEntity;
import com.iwos.warehouseorchestrator.infrastructure.persistence.repository.WarehouseOutboxEventRepository;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class WarehouseOutboxEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(WarehouseOutboxEventPublisher.class);
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_PUBLISHED = "PUBLISHED";
    private static final String STATUS_FAILED = "FAILED";

    private final WarehouseOutboxEventRepository warehouseOutboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final WarehouseOrchestratorServiceProperties properties;
    private final WarehouseOrchestratorMetrics warehouseMetrics;

    public WarehouseOutboxEventPublisher(
            WarehouseOutboxEventRepository warehouseOutboxEventRepository,
            KafkaTemplate<String, String> kafkaTemplate,
            WarehouseOrchestratorServiceProperties properties,
            WarehouseOrchestratorMetrics warehouseMetrics
    ) {
        this.warehouseOutboxEventRepository = warehouseOutboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.properties = properties;
        this.warehouseMetrics = warehouseMetrics;
    }

    @Scheduled(fixedDelayString = "${iwos.warehouse-orchestrator.kafka.outbox-poll-interval:PT5S}")
    public void publishPendingEvents() {
        List<WarehouseOutboxEventEntity> pendingEvents = warehouseOutboxEventRepository
                .findByStatusOrderByCreatedAtAsc(STATUS_PENDING)
                .stream()
                .limit(properties.getKafka().getOutboxBatchSize())
                .toList();

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.info("Publishing {} pending warehouse outbox events", pendingEvents.size());
        for (WarehouseOutboxEventEntity event : pendingEvents) {
            publishEvent(event);
        }
    }

    private void publishEvent(WarehouseOutboxEventEntity event) {
        String topic = properties.getKafka().getOutboxTopic();
        String key = event.getAggregateId().toString();
        String value = event.getPayload();

        try {
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, key, value);
            future.whenComplete((result, exception) -> {
                if (exception == null) {
                    warehouseOutboxEventRepository.updateStatus(event.getOutboxEventId(), STATUS_PUBLISHED, null);
                    warehouseMetrics.recordOutboxPublish("published");
                    log.debug("Published warehouse event {} to topic {} offset {}",
                            event.getOutboxEventId(),
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().offset());
                } else {
                    warehouseOutboxEventRepository.updateStatus(
                            event.getOutboxEventId(),
                            STATUS_FAILED,
                            trimLastError(exception.getMessage())
                    );
                    warehouseMetrics.recordOutboxPublish("failed");
                    log.error("Failed to publish warehouse event {}: {}",
                            event.getOutboxEventId(), exception.getMessage());
                }
            });
        } catch (Exception exception) {
            warehouseOutboxEventRepository.updateStatus(
                    event.getOutboxEventId(),
                    STATUS_FAILED,
                    trimLastError(exception.getMessage())
            );
            warehouseMetrics.recordOutboxPublish("failed");
            log.error("Exception while sending warehouse event {}", event.getOutboxEventId(), exception);
        }
    }

    private String trimLastError(String lastError) {
        if (lastError == null || lastError.isBlank()) {
            return null;
        }
        return lastError.length() <= 1000 ? lastError : lastError.substring(0, 1000);
    }
}
