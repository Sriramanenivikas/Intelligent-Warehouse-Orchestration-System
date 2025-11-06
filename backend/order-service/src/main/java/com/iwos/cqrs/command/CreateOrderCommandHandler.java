package com.iwos.cqrs.command;

import com.iwos.domain.Order;
import com.iwos.domain.OrderStatus;
import com.iwos.event.OrderCreatedEvent;
import com.iwos.eventsourcing.EventStore;
import com.iwos.repository.OrderWriteRepository;
import com.iwos.warehouse.WarehouseAllocationService;
import com.iwos.warehouse.Warehouse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * CQRS COMMAND HANDLER
 *
 * Responsibilities:
 * 1. Validate command
 * 2. Execute business logic
 * 3. Save to write database (PostgreSQL)
 * 4. Store event (Event Sourcing)
 * 5. Publish event to Kafka
 *
 * This is the WRITE SIDE of CQRS
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CreateOrderCommandHandler {

    private final OrderWriteRepository orderWriteRepository;
    private final EventStore eventStore;
    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;
    private final WarehouseAllocationService warehouseAllocationService;

    /**
     * Handle CreateOrderCommand
     *
     * Transaction boundary: DB write + Event store
     * Kafka publish is outside transaction (at-least-once delivery)
     *
     * @param command The command to execute
     * @return Order ID
     */
    @Transactional
    public String handle(CreateOrderCommand command) {
        log.info("🎯 Handling CreateOrderCommand for customer: {}", command.getCustomerId());

        // 1. Validate command
        command.validate();

        // 2. Find optimal warehouse using geospatial algorithm
        log.info("📍 Finding optimal warehouse for location: ({}, {})",
            command.getDeliveryAddress().getLatitude(),
            command.getDeliveryAddress().getLongitude());

        Warehouse optimalWarehouse = warehouseAllocationService.findOptimalWarehouse(
            command.getItems(),
            command.getDeliveryAddress().getLatitude(),
            command.getDeliveryAddress().getLongitude(),
            command.getDeliveryType()
        );

        if (optimalWarehouse == null) {
            log.error("❌ No warehouse available for order");
            throw new NoWarehouseAvailableException(
                "No warehouse with required inventory found within delivery range"
            );
        }

        log.info("✅ Allocated warehouse: {} (distance: {} km)",
            optimalWarehouse.getName(),
            optimalWarehouse.getDistanceFromCustomer());

        // 3. Create domain entity
        String orderId = UUID.randomUUID().toString();
        String orderNumber = generateOrderNumber();

        Order order = Order.builder()
            .id(orderId)
            .orderNumber(orderNumber)
            .customerId(command.getCustomerId())
            .warehouseId(optimalWarehouse.getId())
            .status(OrderStatus.PENDING)
            .totalAmount(command.getTotalAmount())
            .deliveryType(command.getDeliveryType())
            .paymentMethod(command.getPaymentMethod())
            .createdAt(Instant.now())
            .build();

        // 4. Save to write database (PostgreSQL - normalized)
        orderWriteRepository.save(order);
        log.info("💾 Order saved to write database: {}", orderNumber);

        // 5. Create domain event
        OrderCreatedEvent event = OrderCreatedEvent.builder()
            .eventId(UUID.randomUUID().toString())
            .eventType("OrderCreated")
            .aggregateId(orderId)
            .aggregateType("Order")
            .orderId(orderId)
            .orderNumber(orderNumber)
            .customerId(command.getCustomerId())
            .warehouseId(optimalWarehouse.getId())
            .warehouseName(optimalWarehouse.getName())
            .items(command.getItems())
            .deliveryAddress(command.getDeliveryAddress())
            .totalAmount(command.getTotalAmount())
            .deliveryType(command.getDeliveryType())
            .occurredAt(Instant.now())
            .version(1L)
            .build();

        // 6. Store event (Event Sourcing)
        eventStore.save(event);
        log.info("📜 Event stored in event store: {}", event.getEventType());

        // 7. Publish event to Kafka (triggers read model update + saga)
        kafkaTemplate.send("order.events", orderId, event)
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("📤 Event published to Kafka: topic=order.events, partition={}",
                        result.getRecordMetadata().partition());
                } else {
                    log.error("❌ Failed to publish event to Kafka", ex);
                    // TODO: Store in dead letter queue for retry
                }
            });

        log.info("🎉 Order created successfully: {} (warehouse: {})",
            orderNumber, optimalWarehouse.getName());

        return orderId;
    }

    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis();
    }
}

class NoWarehouseAvailableException extends RuntimeException {
    public NoWarehouseAvailableException(String message) {
        super(message);
    }
}
