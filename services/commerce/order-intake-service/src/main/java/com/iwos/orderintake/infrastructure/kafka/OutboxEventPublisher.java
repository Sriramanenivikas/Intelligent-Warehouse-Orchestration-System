package com.iwos.orderintake.infrastructure.kafka;

import com.iwos.orderintake.infrastructure.config.OrderIntakeServiceProperties;
import com.iwos.orderintake.infrastructure.observability.OrderIntakeMetrics;
import com.iwos.orderintake.infrastructure.persistence.entity.OutboxEventEntity;
import com.iwos.orderintake.infrastructure.persistence.repository.OutboxEventRepository;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Polls the outbox table and publishes pending events to Kafka.
 * Uses at-least-once delivery semantics.
 */
@Component
public class OutboxEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxEventPublisher.class);
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_PUBLISHED = "PUBLISHED";
    private static final String STATUS_FAILED = "FAILED";

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OrderIntakeServiceProperties properties;
    private final OrderIntakeMetrics orderIntakeMetrics;

    public OutboxEventPublisher(
            OutboxEventRepository outboxEventRepository,
            KafkaTemplate<String, String> kafkaTemplate,
            OrderIntakeServiceProperties properties,
            OrderIntakeMetrics orderIntakeMetrics
    ) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.properties = properties;
        this.orderIntakeMetrics = orderIntakeMetrics;
    }

    @Scheduled(fixedDelayString = "${iwos.order-intake.outbox.poll-interval:PT5S}")
    public void publishPendingEvents() {
        List<OutboxEventEntity> pendingEvents = outboxEventRepository
                .findByStatusOrderByCreatedAtAsc(STATUS_PENDING)
                .stream()
                .limit(properties.getOutbox().getBatchSize())
                .toList();

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.info("Publishing {} pending outbox events", pendingEvents.size());

        for (OutboxEventEntity event : pendingEvents) {
            publishEvent(event);
        }
    }

    @Transactional
    public void publishEvent(OutboxEventEntity event) {
        String topic = properties.getOutbox().getTopic();
        String key = event.getAggregateId().toString();
        String value = event.getPayload();

        try {
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, key, value);
            
            future.whenComplete((result, exception) -> {
                if (exception == null) {
                    markAsPublished(event);
                    orderIntakeMetrics.recordOutboxPublish("published");
                    log.debug("Published event {} to topic {} partition {} offset {}",
                            event.getOutboxEventId(),
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                } else {
                    markAsFailed(event, exception.getMessage());
                    orderIntakeMetrics.recordOutboxPublish("failed");
                    log.error("Failed to publish event {} to Kafka: {}",
                            event.getOutboxEventId(), exception.getMessage());
                }
            });
        } catch (Exception exception) {
            markAsFailed(event, exception.getMessage());
            orderIntakeMetrics.recordOutboxPublish("failed");
            log.error("Exception while sending event {} to Kafka", event.getOutboxEventId(), exception);
        }
    }

    private void markAsPublished(OutboxEventEntity event) {
        outboxEventRepository.updateStatus(event.getOutboxEventId(), STATUS_PUBLISHED);
    }

    private void markAsFailed(OutboxEventEntity event, String reason) {
        outboxEventRepository.updateStatus(event.getOutboxEventId(), STATUS_FAILED);
    }
}
