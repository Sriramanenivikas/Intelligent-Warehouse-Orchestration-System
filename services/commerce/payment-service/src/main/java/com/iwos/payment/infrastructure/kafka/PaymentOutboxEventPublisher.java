package com.iwos.payment.infrastructure.kafka;

import com.iwos.payment.infrastructure.config.PaymentServiceProperties;
import com.iwos.payment.infrastructure.persistence.entity.PaymentOutboxEventEntity;
import com.iwos.payment.infrastructure.persistence.repository.PaymentOutboxEventRepository;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PaymentOutboxEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PaymentOutboxEventPublisher.class);
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_PUBLISHED = "PUBLISHED";

    private final PaymentOutboxEventRepository paymentOutboxEventRepository;
    private final PaymentServiceProperties properties;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public PaymentOutboxEventPublisher(
            PaymentOutboxEventRepository paymentOutboxEventRepository,
            PaymentServiceProperties properties,
            KafkaTemplate<String, String> kafkaTemplate
    ) {
        this.paymentOutboxEventRepository = paymentOutboxEventRepository;
        this.properties = properties;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelayString = "${iwos.payment.outbox.poll-interval:PT5S}")
    @Transactional
    public void publishPendingEvents() {
        List<PaymentOutboxEventEntity> pendingEvents = paymentOutboxEventRepository.findByStatusOrderByCreatedAtAsc(
                STATUS_PENDING,
                PageRequest.of(0, properties.getOutboxBatchSize())
        );
        if (pendingEvents.isEmpty()) {
            return;
        }

        log.info("Publishing {} pending payment outbox events", pendingEvents.size());
        for (PaymentOutboxEventEntity event : pendingEvents) {
            try {
                kafkaTemplate.send(properties.getOutboxTopic(), event.getAggregateId().toString(), event.getPayload());
                paymentOutboxEventRepository.markPublished(event.getOutboxEventId(), STATUS_PUBLISHED);
            } catch (Exception exception) {
                paymentOutboxEventRepository.markFailed(event.getOutboxEventId(), STATUS_PENDING, exception.getMessage());
                log.warn("Failed to publish payment outbox event {}", event.getOutboxEventId(), exception);
            }
        }
    }
}
