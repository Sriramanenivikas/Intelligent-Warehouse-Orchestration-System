package com.iwos.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Order Entity
 * Complete implementation with all required fields
 */
@Entity
@Table(name = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String orderId;  // UUID string for external reference

    @Column(unique = true, nullable = false)
    private String orderNumber;  // ORD-123456789

    // Customer Information
    @Column(nullable = false)
    private String customerId;

    private String customerName;

    private String customerEmail;

    private String customerPhone;

    // Warehouse Information
    private String warehouseId;

    private String warehouseName;

    private Double distanceKm;

    private Integer estimatedDeliveryMinutes;

    // Order Details
    @Column(nullable = false)
    private String status;  // PENDING, CONFIRMED, PICKED, PACKED, SHIPPED, DELIVERED, CANCELLED

    private String deliveryType;  // EXPRESS, STANDARD

    private String paymentMethod;  // COD, ONLINE, CARD

    private Integer totalItems;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    // Delivery Address Fields
    private String deliveryLine1;

    private String deliveryLine2;

    private String deliveryCity;

    private String deliveryState;

    private String deliveryPincode;

    private Double deliveryLatitude;

    private Double deliveryLongitude;

    // Order Items - One-to-Many relationship
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    // Assignment
    private Long assignedTo;  // Warehouse staff/picker ID

    // Audit Fields
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    /**
     * Helper method to add order item
     */
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    /**
     * Helper method to remove order item
     */
    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
    }
}
