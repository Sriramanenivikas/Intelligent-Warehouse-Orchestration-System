package com.iwos.cqrs.command;

import com.iwos.domain.Order;
import com.iwos.domain.OrderStatus;
import com.iwos.entity.OrderItem;
import com.iwos.event.OrderCreatedEvent;
import com.iwos.eventsourcing.EventStore;
import com.iwos.repository.OrderRepository;
import com.iwos.repository.OrderWriteRepository;
import com.iwos.warehouse.WarehouseAllocationService;
import com.iwos.warehouse.Warehouse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

    private final OrderRepository orderRepository;
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

        // 3. Calculate total amount if not provided
        BigDecimal totalAmount = command.getTotalAmount();
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) == 0) {
            totalAmount = calculateTotalAmount(command.getItems());
        }

        // 4. Create JPA entity with all fields
        String orderId = UUID.randomUUID().toString();
        String orderNumber = generateOrderNumber();
        LocalDateTime now = LocalDateTime.now();

        com.iwos.entity.Order orderEntity = com.iwos.entity.Order.builder()
                .orderId(orderId)
                .orderNumber(orderNumber)
                .customerId(command.getCustomerId())
                .customerName(command.getCustomerName())
                .customerEmail(command.getCustomerEmail())
                .customerPhone(command.getCustomerPhone())
                .warehouseId(optimalWarehouse.getId())
                .warehouseName(optimalWarehouse.getName())
                .distanceKm(optimalWarehouse.getDistanceFromCustomer())
                .estimatedDeliveryMinutes(optimalWarehouse.getEstimatedDeliveryMinutes())
                .status(OrderStatus.PENDING.name())
                .totalAmount(totalAmount)
                .totalItems(command.getItems().size())
                .deliveryType(command.getDeliveryType())
                .paymentMethod(command.getPaymentMethod())
                .deliveryLine1(command.getDeliveryAddress().getLine1())
                .deliveryLine2(command.getDeliveryAddress().getLine2())
                .deliveryCity(command.getDeliveryAddress().getCity())
                .deliveryState(command.getDeliveryAddress().getState())
                .deliveryPincode(command.getDeliveryAddress().getPincode())
                .deliveryLatitude(command.getDeliveryAddress().getLatitude())
                .deliveryLongitude(command.getDeliveryAddress().getLongitude())
                .createdAt(now)
                .updatedAt(now)
                .items(new ArrayList<>())
                .build();

        // 5. Create order items
        for (CreateOrderCommand.OrderItemDTO itemDto : command.getItems()) {
            BigDecimal itemTotalPrice = itemDto.getUnitPrice()
                    .multiply(BigDecimal.valueOf(itemDto.getQuantity()));

            OrderItem orderItem = OrderItem.builder()
                    .order(orderEntity)
                    .sku(itemDto.getSku())
                    .productName(itemDto.getProductName())
                    .quantity(itemDto.getQuantity())
                    .quantityOrdered(itemDto.getQuantity())
                    .unitPrice(itemDto.getUnitPrice())
                    .totalPrice(itemTotalPrice)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            orderEntity.getItems().add(orderItem);
        }

        // 6. Save to write database (PostgreSQL - with items)
        com.iwos.entity.Order savedOrder = orderRepository.save(orderEntity);
        log.info("💾 Order saved to database: {} with {} items", orderNumber, savedOrder.getItems().size());

        // 7. Create domain event
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("OrderCreated")
                .aggregateId(orderId)
                .aggregateType("Order")
                .orderId(orderId)
                .orderNumber(orderNumber)
                .customerId(command.getCustomerId())
                .customerName(command.getCustomerName())
                .customerEmail(command.getCustomerEmail())
                .customerPhone(command.getCustomerPhone())
                .warehouseId(optimalWarehouse.getId())
                .warehouseName(optimalWarehouse.getName())
                .distanceKm(optimalWarehouse.getDistanceFromCustomer())
                .estimatedDeliveryMinutes(optimalWarehouse.getEstimatedDeliveryMinutes())
                .items(command.getItems())
                .deliveryAddress(command.getDeliveryAddress())
                .totalAmount(totalAmount)
                .deliveryType(command.getDeliveryType())
                .paymentMethod(command.getPaymentMethod())
                .occurredAt(Instant.now())
                .version(1L)
                .build();

        // 8. Store event (Event Sourcing) - using overloaded method with all metadata
        eventStore.save(
                event.getEventId(),
                event.getAggregateType(),
                event.getAggregateId(),
                event.getEventType(),
                event.getVersion(),
                event.getOccurredAt(),
                event
        );
        log.info("📜 Event stored in event store: {}", event.getEventType());

        // 9. Publish event to Kafka (triggers read model update + saga)
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

        log.info("🎉 Order created successfully: {} (warehouse: {}, distance: {} km, ETA: {} mins)",
                orderNumber, optimalWarehouse.getName(),
                String.format("%.2f", optimalWarehouse.getDistanceFromCustomer()),
                optimalWarehouse.getEstimatedDeliveryMinutes());

        return orderId;
    }

    /**
     * Calculate total amount from order items
     */
    private BigDecimal calculateTotalAmount(List<CreateOrderCommand.OrderItemDTO> items) {
        return items.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Generate unique order number
     */
    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis();
    }
}

class NoWarehouseAvailableException extends RuntimeException {
    public NoWarehouseAvailableException(String message) {
        super(message);
    }
}
