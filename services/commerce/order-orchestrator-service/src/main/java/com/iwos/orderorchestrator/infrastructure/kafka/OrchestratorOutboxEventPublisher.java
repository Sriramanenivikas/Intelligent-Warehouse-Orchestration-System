package com.iwos.orderorchestrator.infrastructure.kafka;

import com.iwos.orderorchestrator.infrastructure.config.OrderOrchestratorServiceProperties;
import com.iwos.orderorchestrator.infrastructure.observability.OrderWorkflowMetrics;
import com.iwos.orderorchestrator.infrastructure.persistence.entity.OrderOrchestratorOutboxEventEntity;
import com.iwos.orderorchestrator.infrastructure.persistence.repository.OrderOrchestratorOutboxEventRepository;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Polls the orchestrator outbox table and publishes pending events to Kafka.
 */
@Component
public class OrchestratorOutboxEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OrchestratorOutboxEventPublisher.class);
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_PUBLISHED = "PUBLISHED";
    private static final String STATUS_FAILED = "FAILED";

    private final OrderOrchestratorOutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OrderOrchestratorServiceProperties properties;
    private final OrderWorkflowMetrics orderWorkflowMetrics;

    public OrchestratorOutboxEventPublisher(
            OrderOrchestratorOutboxEventRepository outboxEventRepository,
            KafkaTemplate<String, String> kafkaTemplate,
            OrderOrchestratorServiceProperties properties,
            OrderWorkflowMetrics orderWorkflowMetrics
    ) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.properties = properties;
        this.orderWorkflowMetrics = orderWorkflowMetrics;
    }

    @Scheduled(fixedDelayString = "PT5S")
    public void publishPendingEvents() {
        List<OrderOrchestratorOutboxEventEntity> pendingEvents = outboxEventRepository
                .findByStatusOrderByCreatedAtAsc(STATUS_PENDING)
                .stream()
                .limit(50)
                .toList();

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.info("Publishing {} pending orchestrator outbox events", pendingEvents.size());

        for (OrderOrchestratorOutboxEventEntity event : pendingEvents) {
            publishEvent(event);
        }
    }

    private void publishEvent(OrderOrchestratorOutboxEventEntity event) {
        String topic = properties.getKafka().getOutboxTopic();
        String key = event.getAggregateId().toString();
        String value = event.getPayload();

        try {
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, key, value);

            future.whenComplete((result, exception) -> {
                if (exception == null) {
                    outboxEventRepository.updateStatus(event.getOutboxEventId(), STATUS_PUBLISHED);
                    orderWorkflowMetrics.recordOutboxPublish("published");
                    log.debug("Published orchestrator event {} to topic {} offset {}",
                            event.getOutboxEventId(),
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().offset());
                } else {
                    outboxEventRepository.updateStatus(event.getOutboxEventId(), STATUS_FAILED);
                    orderWorkflowMetrics.recordOutboxPublish("failed");
                    log.error("Failed to publish orchestrator event {}: {}",
                            event.getOutboxEventId(), exception.getMessage());
                }
            });
        } catch (Exception exception) {
            outboxEventRepository.updateStatus(event.getOutboxEventId(), STATUS_FAILED);
            orderWorkflowMetrics.recordOutboxPublish("failed");
            log.error("Exception while sending orchestrator event {}", event.getOutboxEventId(), exception);
        }
    }
}
