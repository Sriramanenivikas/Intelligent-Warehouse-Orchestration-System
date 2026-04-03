package com.iwos.inventoryledger.infrastructure.kafka;

import com.iwos.inventoryledger.infrastructure.config.InventoryLedgerServiceProperties;
import com.iwos.inventoryledger.infrastructure.observability.InventoryMetrics;
import com.iwos.inventoryledger.infrastructure.persistence.entity.InventoryOutboxEventEntity;
import com.iwos.inventoryledger.infrastructure.persistence.repository.InventoryOutboxEventRepository;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Polls the inventory outbox table and publishes pending events to Kafka.
 */
@Component
public class InventoryOutboxEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(InventoryOutboxEventPublisher.class);
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_PUBLISHED = "PUBLISHED";
    private static final String STATUS_FAILED = "FAILED";

    private final InventoryOutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final InventoryLedgerServiceProperties properties;
    private final InventoryMetrics inventoryMetrics;

    public InventoryOutboxEventPublisher(
            InventoryOutboxEventRepository outboxEventRepository,
            KafkaTemplate<String, String> kafkaTemplate,
            InventoryLedgerServiceProperties properties,
            InventoryMetrics inventoryMetrics
    ) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.properties = properties;
        this.inventoryMetrics = inventoryMetrics;
    }

    @Scheduled(fixedDelayString = "${iwos.inventory-ledger.outbox.poll-interval:PT5S}")
    public void publishPendingEvents() {
        List<InventoryOutboxEventEntity> pendingEvents = outboxEventRepository
                .findByStatusOrderByCreatedAtAsc(STATUS_PENDING)
                .stream()
                .limit(properties.getOutbox().getBatchSize())
                .toList();

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.info("Publishing {} pending inventory outbox events", pendingEvents.size());

        for (InventoryOutboxEventEntity event : pendingEvents) {
            publishEvent(event);
        }
    }

    private void publishEvent(InventoryOutboxEventEntity event) {
        String topic = properties.getOutbox().getTopic();
        String key = event.getAggregateId().toString();
        String value = event.getPayload();

        try {
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, key, value);

            future.whenComplete((result, exception) -> {
                if (exception == null) {
                    outboxEventRepository.updateStatus(event.getOutboxEventId(), STATUS_PUBLISHED);
                    inventoryMetrics.recordOutboxPublish("published");
                    log.debug("Published inventory event {} to topic {} offset {}",
                            event.getOutboxEventId(),
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().offset());
                } else {
                    outboxEventRepository.updateStatus(event.getOutboxEventId(), STATUS_FAILED);
                    inventoryMetrics.recordOutboxPublish("failed");
                    log.error("Failed to publish inventory event {}: {}",
                            event.getOutboxEventId(), exception.getMessage());
                }
            });
        } catch (Exception exception) {
            outboxEventRepository.updateStatus(event.getOutboxEventId(), STATUS_FAILED);
            inventoryMetrics.recordOutboxPublish("failed");
            log.error("Exception while sending inventory event {}", event.getOutboxEventId(), exception);
        }
    }
}
