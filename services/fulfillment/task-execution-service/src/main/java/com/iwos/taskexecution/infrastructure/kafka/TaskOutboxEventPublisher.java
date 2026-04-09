package com.iwos.taskexecution.infrastructure.kafka;

import com.iwos.taskexecution.infrastructure.config.TaskExecutionServiceProperties;
import com.iwos.taskexecution.infrastructure.observability.TaskExecutionMetrics;
import com.iwos.taskexecution.infrastructure.persistence.entity.TaskOutboxEventEntity;
import com.iwos.taskexecution.infrastructure.persistence.repository.TaskOutboxEventRepository;
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
public class TaskOutboxEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(TaskOutboxEventPublisher.class);
    private static final int BATCH_SIZE = 50;
    private static final int MAX_ATTEMPTS = 5;

    private final TaskOutboxEventRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final TaskExecutionMetrics metrics;
    private final String producerTopic;

    public TaskOutboxEventPublisher(
            TaskOutboxEventRepository outboxRepository,
            KafkaTemplate<String, String> kafkaTemplate,
            TaskExecutionMetrics metrics,
            TaskExecutionServiceProperties properties
    ) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.metrics = metrics;
        this.producerTopic = properties.kafka().producerTopic();
    }

    @Scheduled(fixedDelayString = "${task-execution-service.outbox.poll-interval-ms:1000}")
    public void publishPendingEvents() {
        List<TaskOutboxEventEntity> pending = outboxRepository.findPendingEvents(PageRequest.of(0, BATCH_SIZE));

        for (TaskOutboxEventEntity event : pending) {
            publishEvent(event);
        }
    }

    @Transactional
    public void publishEvent(TaskOutboxEventEntity event) {
        try {
            String key = event.getAggregateId().toString();
            ProducerRecord<String, String> record = new ProducerRecord<>(producerTopic, key, event.getPayload());

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

    private void handlePublishSuccess(TaskOutboxEventEntity event) {
        try {
            event.setStatus("PUBLISHED");
            event.setPublishedAt(Instant.now());
            event.setUpdatedAt(Instant.now());
            outboxRepository.save(event);
            metrics.incrementOutboxPublished();

            log.debug("Published outbox event: id={}, type={}", event.getOutboxEventId(), event.getEventType());
        } catch (Exception e) {
            log.error("Failed to update outbox event status: {}", event.getOutboxEventId(), e);
        }
    }

    private void handlePublishFailure(TaskOutboxEventEntity event, Throwable ex) {
        try {
            event.setAttempts(event.getAttempts() + 1);
            event.setLastError(ex.getMessage() != null ? ex.getMessage().substring(0, Math.min(ex.getMessage().length(), 1000)) : "Unknown error");
            event.setUpdatedAt(Instant.now());

            if (event.getAttempts() >= MAX_ATTEMPTS) {
                event.setStatus("FAILED");
                log.error("Outbox event permanently failed after {} attempts: id={}", MAX_ATTEMPTS, event.getOutboxEventId());
            }

            outboxRepository.save(event);
            metrics.incrementOutboxFailed();

        } catch (Exception e) {
            log.error("Failed to update failed outbox event: {}", event.getOutboxEventId(), e);
        }
    }
}
