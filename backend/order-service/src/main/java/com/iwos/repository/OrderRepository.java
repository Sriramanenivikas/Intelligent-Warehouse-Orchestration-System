package com.iwos.repository;

import com.iwos.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Order Repository
 * Complete implementation with custom query methods
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Find order by external order ID (UUID string)
     */
    Optional<Order> findByOrderId(String orderId);

    /**
     * Find order by order number
     */
    Optional<Order> findByOrderNumber(String orderNumber);

    /**
     * Find all orders by status
     */
    List<Order> findByStatus(String status);

    /**
     * Find all orders by customer ID
     */
    List<Order> findByCustomerId(String customerId);

    /**
     * Find all orders by customer ID and status
     */
    List<Order> findByCustomerIdAndStatus(String customerId, String status);

    /**
     * Find all orders by warehouse ID
     */
    List<Order> findByWarehouseId(String warehouseId);

    /**
     * Find all orders by warehouse ID and status
     */
    List<Order> findByWarehouseIdAndStatus(String warehouseId, String status);

    /**
     * Find all orders assigned to a specific person
     */
    List<Order> findByAssignedTo(Long assignedTo);

    /**
     * Check if order exists by order ID
     */
    boolean existsByOrderId(String orderId);

    /**
     * Check if order exists by order number
     */
    boolean existsByOrderNumber(String orderNumber);

    /**
     * Find orders by status ordered by creation date (descending)
     */
    List<Order> findByStatusOrderByCreatedAtDesc(String status);

    /**
     * Find orders by customer ordered by creation date (descending)
     */
    List<Order> findByCustomerIdOrderByCreatedAtDesc(String customerId);

    /**
     * Count orders by status
     */
    long countByStatus(String status);

    /**
     * Count orders by customer ID
     */
    long countByCustomerId(String customerId);

    /**
     * Find orders by delivery pincode
     */
    List<Order> findByDeliveryPincode(String pincode);
}
