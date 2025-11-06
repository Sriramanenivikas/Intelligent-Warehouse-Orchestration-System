package com.iwos.repository;

import com.iwos.domain.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * Order Write Repository for CQRS Write Side
 *
 * Uses direct JDBC for write operations to avoid JPA complexity
 * in the command handler. This provides better control over transactions
 * and performance.
 *
 * For reads, use the regular JPA OrderRepository.
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class OrderWriteRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Save order to write database (PostgreSQL)
     *
     * @param order Domain order to save
     * @return Number of rows affected
     */
    public int save(Order order) {
        String sql = """
            INSERT INTO orders (
                order_id, order_number, customer_id, customer_name,
                customer_email, customer_phone, warehouse_id, status,
                total_amount, delivery_type, payment_method,
                created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        Instant now = order.getCreatedAt() != null ? order.getCreatedAt() : Instant.now();

        int rowsAffected = jdbcTemplate.update(sql,
                order.getId(),
                order.getOrderNumber(),
                order.getCustomerId(),
                order.getCustomerName(),
                order.getCustomerEmail(),
                order.getCustomerPhone(),
                order.getWarehouseId(),
                order.getStatus().name(),
                order.getTotalAmount(),
                order.getDeliveryType(),
                order.getPaymentMethod(),
                Timestamp.from(now),
                Timestamp.from(now)
        );

        log.info("💾 Saved order {} to write database", order.getOrderNumber());
        return rowsAffected;
    }

    /**
     * Update order status
     *
     * @param orderId Order ID
     * @param status New status
     * @return Number of rows affected
     */
    public int updateStatus(String orderId, String status) {
        String sql = """
            UPDATE orders
            SET status = ?, updated_at = ?
            WHERE order_id = ?
            """;

        return jdbcTemplate.update(sql, status, Timestamp.from(Instant.now()), orderId);
    }

    /**
     * Check if order exists
     *
     * @param orderId Order ID
     * @return true if exists
     */
    public boolean exists(String orderId) {
        String sql = "SELECT COUNT(*) FROM orders WHERE order_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, orderId);
        return count != null && count > 0;
    }
}
