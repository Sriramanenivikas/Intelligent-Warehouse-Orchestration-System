package com.iwos.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Order Event Publisher
 *
 * Architecture Pattern: Event-Driven Choreography
 *
 * Responsibilities:
 * - Publishes order events to Kafka
 * - Other services subscribe and react independently
 * - No direct coupling between services
 *
 * How it works:
 * 1. Order Service creates order
 * 2. Publishes "order.created" event to Kafka
 * 3. Inventory Service listens and reserves stock
 * 4. Warehouse Service listens and assigns zone
 * 5. Notification Service listens and sends email
 *
 * Benefits:
 * - Loose coupling
 * - Services can be added/removed without changing Order Service
 * - Async processing (non-blocking)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventPublisher {

    private static final String ORDER_EVENTS_TOPIC = "order.events";

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    /**
     * Publish order created event
     *
     * @param event Order event to publish
     */
    public void publishOrderCreated(OrderEvent event) {
        log.info("📤 Publishing event: {} for order: {}", event.getEventType(), event.getOrderNumber());

        kafkaTemplate.send(ORDER_EVENTS_TOPIC, event.getOrderId().toString(), event)
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("✅ Event published successfully: {} to partition: {}",
                        event.getEventType(),
                        result.getRecordMetadata().partition());
                } else {
                    log.error("❌ Failed to publish event: {}", event.getEventType(), ex);
                }
            });
    }

    /**
     * Publish order cancelled event
     */
    public void publishOrderCancelled(OrderEvent event) {
        log.info("📤 Publishing cancellation event for order: {}", event.getOrderNumber());

        kafkaTemplate.send(ORDER_EVENTS_TOPIC, event.getOrderId().toString(), event)
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("✅ Cancellation event published successfully");
                } else {
                    log.error("❌ Failed to publish cancellation event", ex);
                }
            });
    }

    /**
     * Publish order status changed event
     */
    public void publishOrderStatusChanged(OrderEvent event) {
        log.info("📤 Publishing status change: {} -> {}", event.getOrderNumber(), event.getStatus());

        kafkaTemplate.send(ORDER_EVENTS_TOPIC, event.getOrderId().toString(), event);
    }
}
