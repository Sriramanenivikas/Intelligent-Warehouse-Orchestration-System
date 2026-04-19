package com.iwos.shipmentnetwork.infrastructure.kafka;

import com.iwos.shipmentnetwork.infrastructure.config.ShipmentNetworkServiceProperties;
import com.iwos.shipmentnetwork.infrastructure.observability.ShipmentNetworkMetrics;
import com.iwos.shipmentnetwork.infrastructure.persistence.entity.NetworkOutboxEventEntity;
import com.iwos.shipmentnetwork.infrastructure.persistence.repository.NetworkOutboxEventRepository;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ShipmentNetworkOutboxEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(ShipmentNetworkOutboxEventPublisher.class);
    private static final int BATCH_SIZE = 50;
    private static final int MAX_ATTEMPTS = 5;

    private final NetworkOutboxEventRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ShipmentNetworkMetrics metrics;
    private final String producerTopic;

    public ShipmentNetworkOutboxEventPublisher(
            NetworkOutboxEventRepository outboxRepository,
            KafkaTemplate<String, String> kafkaTemplate,
            ShipmentNetworkMetrics metrics,
            ShipmentNetworkServiceProperties properties
    ) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.metrics = metrics;
        this.producerTopic = properties.kafka().producerTopic();
    }

    @Scheduled(fixedDelayString = "${shipment-network-service.outbox.poll-interval-ms:1000}")
    public void publishPendingEvents() {
        List<NetworkOutboxEventEntity> pending = outboxRepository.findPendingEvents(PageRequest.of(0, BATCH_SIZE));
        for (NetworkOutboxEventEntity event : pending) {
            publishEvent(event);
        }
    }

    @Transactional
    public void publishEvent(NetworkOutboxEventEntity event) {
        try {
            ProducerRecord<String, String> record = new ProducerRecord<>(
                    producerTopic,
                    event.getAggregateId().toString(),
                    event.getPayload()
            );
            CompletableFuture<?> future = kafkaTemplate.send(record);
            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    handlePublishFailure(event, ex);
                } else {
                    handlePublishSuccess(event);
                }
            });
        } catch (Exception e) {
            handlePublishFailure(event, e);
        }
    }

    private void handlePublishSuccess(NetworkOutboxEventEntity event) {
        try {
            event.setStatus("PUBLISHED");
            event.setPublishedAt(Instant.now());
            event.setUpdatedAt(Instant.now());
            outboxRepository.save(event);
            metrics.incrementOutboxPublished();
            log.debug("Published shipment-network outbox event: id={}, type={}", event.getOutboxEventId(), event.getEventType());
        } catch (Exception e) {
            log.error("Failed to mark shipment-network outbox event published: {}", event.getOutboxEventId(), e);
        }
    }

    private void handlePublishFailure(NetworkOutboxEventEntity event, Throwable ex) {
        try {
            event.setAttempts(event.getAttempts() + 1);
            event.setLastError(ex.getMessage() != null ? ex.getMessage().substring(0, Math.min(ex.getMessage().length(), 1000)) : "Unknown error");
            if (event.getAttempts() >= MAX_ATTEMPTS) {
                event.setStatus("FAILED");
            }
            event.setUpdatedAt(Instant.now());
            outboxRepository.save(event);
            metrics.incrementOutboxFailed();
        } catch (Exception e) {
            log.error("Failed to update failed shipment-network outbox event: {}", event.getOutboxEventId(), e);
        }
    }
}
