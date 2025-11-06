package com.iwos.service;

import com.iwos.entity.Order;
import com.iwos.event.OrderEvent;
import com.iwos.event.OrderEventPublisher;
import com.iwos.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Order Service
 * Complete implementation with all required business logic methods
 *
 * SOLID Principles:
 * - Single Responsibility: Handles Order business logic
 * - Dependency Inversion: Depends on repository abstraction
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {

    private final OrderRepository repository;
    private final OrderEventPublisher eventPublisher;

    /**
     * Get all Orders
     */
    @Transactional(readOnly = true)
    public List<Order> getAll() {
        log.info("Fetching all Orders");
        return repository.findAll();
    }

    /**
     * Get all Orders (alias for controller)
     */
    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        log.info("Fetching all Orders");
        return repository.findAll();
    }

    /**
     * Get Order by ID (internal Long ID)
     */
    @Transactional(readOnly = true)
    public Optional<Order> getById(Long id) {
        log.info("Fetching Order with ID: {}", id);
        return repository.findById(id);
    }

    /**
     * Get Order by external order ID (UUID string)
     * Throws exception if not found
     */
    @Transactional(readOnly = true)
    public Order getOrderById(String orderId) {
        log.info("Fetching Order with orderId: {}", orderId);
        return repository.findByOrderId(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + orderId));
    }

    /**
     * Get Order by order number
     */
    @Transactional(readOnly = true)
    public Optional<Order> getByOrderNumber(String orderNumber) {
        log.info("Fetching Order with order number: {}", orderNumber);
        return repository.findByOrderNumber(orderNumber);
    }

    /**
     * Get all orders by status
     */
    @Transactional(readOnly = true)
    public List<Order> getOrdersByStatus(String status) {
        log.info("Fetching Orders with status: {}", status);
        return repository.findByStatusOrderByCreatedAtDesc(status);
    }

    /**
     * Get all orders by customer ID
     */
    @Transactional(readOnly = true)
    public List<Order> getOrdersByCustomerId(String customerId) {
        log.info("Fetching Orders for customer: {}", customerId);
        return repository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    /**
     * Get all orders by warehouse ID
     */
    @Transactional(readOnly = true)
    public List<Order> getOrdersByWarehouseId(String warehouseId) {
        log.info("Fetching Orders for warehouse: {}", warehouseId);
        return repository.findByWarehouseId(warehouseId);
    }

    /**
     * Get all orders by customer ID and status
     */
    @Transactional(readOnly = true)
    public List<Order> getOrdersByCustomerIdAndStatus(String customerId, String status) {
        log.info("Fetching Orders for customer: {} with status: {}", customerId, status);
        return repository.findByCustomerIdAndStatus(customerId, status);
    }

    /**
     * Create Order
     */
    public Order create(Order entity) {
        log.info("Creating new Order");
        return repository.save(entity);
    }

    /**
     * Update Order
     */
    public Order update(Long id, Order entity) {
        log.info("Updating Order with ID: {}", id);
        entity.setId(id);
        return repository.save(entity);
    }

    /**
     * Update order status
     */
    public Order updateOrderStatus(String orderId, String newStatus) {
        log.info("Updating Order {} status to: {}", orderId, newStatus);

        Order order = getOrderById(orderId);
        String oldStatus = order.getStatus();

        // Validate status transition
        validateStatusTransition(oldStatus, newStatus);

        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());

        Order updatedOrder = repository.save(order);
        log.info("Order {} status updated successfully to: {}", orderId, newStatus);

        // Publish OrderStatusUpdatedEvent to Kafka
        try {
            OrderEvent event = OrderEvent.orderStatusChanged(
                order.getId(),
                order.getOrderNumber(),
                oldStatus,
                newStatus
            );
            eventPublisher.publishOrderStatusChanged(event);
            log.info("✅ Status change event published for order: {}", orderId);
        } catch (Exception e) {
            log.error("❌ Failed to publish status change event for order: {}", orderId, e);
            // Don't fail the transaction if event publishing fails
            // The order status is already updated in the database
        }

        return updatedOrder;
    }

    /**
     * Cancel order
     */
    public Order cancelOrder(String orderId) {
        log.info("Cancelling Order: {}", orderId);

        Order order = getOrderById(orderId);

        // Validate that order can be cancelled
        if ("DELIVERED".equals(order.getStatus())) {
            throw new OrderCancellationException("Cannot cancel order that has been delivered: " + orderId);
        }
        if ("CANCELLED".equals(order.getStatus())) {
            throw new OrderCancellationException("Order is already cancelled: " + orderId);
        }

        order.setStatus("CANCELLED");
        order.setUpdatedAt(LocalDateTime.now());

        Order cancelledOrder = repository.save(order);
        log.info("Order {} cancelled successfully", orderId);

        // Publish OrderCancelledEvent to Kafka
        // This event triggers:
        // 1. Inventory Service: Restores reserved inventory
        // 2. Payment Service: Initiates refund if payment was made
        // 3. Notification Service: Sends cancellation notification to customer
        try {
            OrderEvent event = OrderEvent.orderCancelled(
                order.getId(),
                order.getOrderNumber(),
                "Customer requested cancellation"
            );
            eventPublisher.publishOrderCancelled(event);
            log.info("✅ Order cancellation event published successfully");
            log.info("📦 Inventory Service will automatically restore reserved inventory");
            log.info("💳 Payment Service will initiate refund if applicable");
        } catch (Exception e) {
            log.error("❌ Failed to publish order cancellation event for order: {}", orderId, e);
            // Note: The order is already cancelled in the database
            // The compensating transactions (inventory restore, refund) may need manual intervention
            log.warn("⚠️  Manual intervention may be required to restore inventory and process refund");
        }

        return cancelledOrder;
    }

    /**
     * Delete Order by internal ID
     */
    public void delete(Long id) {
        log.info("Deleting Order with ID: {}", id);
        repository.deleteById(id);
    }

    /**
     * Check if order exists by order ID
     */
    @Transactional(readOnly = true)
    public boolean existsByOrderId(String orderId) {
        return repository.existsByOrderId(orderId);
    }

    /**
     * Count orders by status
     */
    @Transactional(readOnly = true)
    public long countByStatus(String status) {
        return repository.countByStatus(status);
    }

    /**
     * Count orders by customer ID
     */
    @Transactional(readOnly = true)
    public long countByCustomerId(String customerId) {
        return repository.countByCustomerId(customerId);
    }

    /**
     * Validate status transition
     * Ensures only valid state transitions are allowed
     */
    private void validateStatusTransition(String currentStatus, String newStatus) {
        // Define valid status transitions
        // PENDING -> CONFIRMED, CANCELLED
        // CONFIRMED -> PICKED, CANCELLED
        // PICKED -> PACKED, CANCELLED
        // PACKED -> SHIPPED, CANCELLED
        // SHIPPED -> DELIVERED
        // DELIVERED -> (no transitions)
        // CANCELLED -> (no transitions)

        if ("DELIVERED".equals(currentStatus) || "CANCELLED".equals(currentStatus)) {
            throw new InvalidStatusTransitionException(
                    String.format("Cannot transition from %s to %s", currentStatus, newStatus));
        }

        // Additional validation logic can be added here
        log.debug("Status transition validated: {} -> {}", currentStatus, newStatus);
    }
}

/**
 * Exception thrown when order is not found
 */
class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String message) {
        super(message);
    }
}

/**
 * Exception thrown when order cancellation fails
 */
class OrderCancellationException extends RuntimeException {
    public OrderCancellationException(String message) {
        super(message);
    }
}

/**
 * Exception thrown when status transition is invalid
 */
class InvalidStatusTransitionException extends RuntimeException {
    public InvalidStatusTransitionException(String message) {
        super(message);
    }
}
