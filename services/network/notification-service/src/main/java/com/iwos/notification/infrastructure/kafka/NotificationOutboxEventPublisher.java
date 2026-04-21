package com.iwos.notification.infrastructure.kafka;

import com.iwos.notification.infrastructure.config.NotificationServiceProperties;
import com.iwos.notification.infrastructure.observability.NotificationMetrics;
import com.iwos.notification.infrastructure.persistence.entity.NotificationOutboxEventEntity;
import com.iwos.notification.infrastructure.persistence.repository.NotificationOutboxEventRepository;
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
public class NotificationOutboxEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(NotificationOutboxEventPublisher.class);
    private static final int BATCH_SIZE = 50;
    private static final int MAX_ATTEMPTS = 5;

    private final NotificationOutboxEventRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final NotificationMetrics metrics;
    private final String producerTopic;

    public NotificationOutboxEventPublisher(
            NotificationOutboxEventRepository outboxRepository,
            KafkaTemplate<String, String> kafkaTemplate,
            NotificationMetrics metrics,
            NotificationServiceProperties properties
    ) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.metrics = metrics;
        this.producerTopic = properties.kafka().producerTopic();
    }

    @Scheduled(fixedDelayString = "${notification-service.outbox.poll-interval-ms:1000}")
    public void publishPendingEvents() {
        List<NotificationOutboxEventEntity> pending = outboxRepository.findPendingEvents(PageRequest.of(0, BATCH_SIZE));
        for (NotificationOutboxEventEntity event : pending) {
            publishEvent(event);
        }
    }

    @Transactional
    public void publishEvent(NotificationOutboxEventEntity event) {
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

    private void handlePublishSuccess(NotificationOutboxEventEntity event) {
        try {
            event.setStatus("PUBLISHED");
            event.setPublishedAt(Instant.now());
            event.setUpdatedAt(Instant.now());
            outboxRepository.save(event);
            metrics.incrementOutboxPublished();
        } catch (Exception e) {
            log.error("Failed to mark notification outbox published: {}", event.getOutboxEventId(), e);
        }
    }

    private void handlePublishFailure(NotificationOutboxEventEntity event, Throwable ex) {
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
            log.error("Failed to update failed notification outbox event: {}", event.getOutboxEventId(), e);
        }
    }
}
