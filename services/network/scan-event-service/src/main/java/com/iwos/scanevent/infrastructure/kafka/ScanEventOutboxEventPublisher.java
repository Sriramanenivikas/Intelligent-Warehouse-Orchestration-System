package com.iwos.scanevent.infrastructure.kafka;

import com.iwos.scanevent.infrastructure.config.ScanEventServiceProperties;
import com.iwos.scanevent.infrastructure.observability.ScanEventMetrics;
import com.iwos.scanevent.infrastructure.persistence.entity.ScanEventOutboxEventEntity;
import com.iwos.scanevent.infrastructure.persistence.repository.ScanEventOutboxEventRepository;
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
public class ScanEventOutboxEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(ScanEventOutboxEventPublisher.class);
    private static final int BATCH_SIZE = 50;
    private static final int MAX_ATTEMPTS = 5;

    private final ScanEventOutboxEventRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ScanEventMetrics metrics;
    private final String producerTopic;

    public ScanEventOutboxEventPublisher(
            ScanEventOutboxEventRepository outboxRepository,
            KafkaTemplate<String, String> kafkaTemplate,
            ScanEventMetrics metrics,
            ScanEventServiceProperties properties
    ) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.metrics = metrics;
        this.producerTopic = properties.kafka().producerTopic();
    }

    @Scheduled(fixedDelayString = "${scan-event-service.outbox.poll-interval-ms:1000}")
    public void publishPendingEvents() {
        List<ScanEventOutboxEventEntity> pending = outboxRepository.findPendingEvents(PageRequest.of(0, BATCH_SIZE));
        for (ScanEventOutboxEventEntity event : pending) {
            publishEvent(event);
        }
    }

    @Transactional
    public void publishEvent(ScanEventOutboxEventEntity event) {
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

    private void handlePublishSuccess(ScanEventOutboxEventEntity event) {
        try {
            event.setStatus("PUBLISHED");
            event.setPublishedAt(Instant.now());
            event.setUpdatedAt(Instant.now());
            outboxRepository.save(event);
            metrics.incrementOutboxPublished();
        } catch (Exception e) {
            log.error("Failed to mark scan-event outbox published: {}", event.getOutboxEventId(), e);
        }
    }

    private void handlePublishFailure(ScanEventOutboxEventEntity event, Throwable ex) {
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
            log.error("Failed to update failed scan-event outbox event: {}", event.getOutboxEventId(), e);
        }
    }
}
