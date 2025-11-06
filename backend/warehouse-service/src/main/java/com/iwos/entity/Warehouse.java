package com.iwos.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Warehouse Entity with Geospatial Support
 * Enhanced for intelligent warehouse allocation
 */
@Entity
@Table(name = "warehouses", indexes = {
    @Index(name = "idx_warehouse_location", columnList = "latitude,longitude"),
    @Index(name = "idx_warehouse_code", columnList = "code"),
    @Index(name = "idx_warehouse_active", columnList = "isActive")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Warehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(length = 100)
    private String country;

    @Column(length = 20)
    private String postalCode;

    // Geospatial fields for PostGIS
    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    // Capacity fields
    private BigDecimal capacitySqm;

    @Column(nullable = false)
    @Builder.Default
    private Integer maxCapacity = 10000;

    @Column(nullable = false)
    @Builder.Default
    private Integer currentLoad = 0;

    // Priority for allocation (1-10, higher is better)
    @Column(nullable = false)
    @Builder.Default
    private Integer priority = 5;

    // Status
    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // Inventory tracking (SKU -> Quantity)
    // Stored as JSONB in PostgreSQL for efficient querying
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Integer> inventory = new HashMap<>();

    // Contact information
    @Column(length = 100)
    private String managerName;

    @Column(length = 20)
    private String contactPhone;

    @Column(length = 100)
    private String contactEmail;

    // Operating hours
    @Column(length = 50)
    private String operatingHours;

    // Audit fields
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Transient field for distance calculation (not persisted)
    @Transient
    private Double distanceFromCustomer;

    /**
     * Check if warehouse is active and not at capacity
     */
    public boolean isAvailable() {
        return isActive && currentLoad < maxCapacity;
    }

    /**
     * Get load percentage (0-100)
     */
    public double getLoadPercentage() {
        if (maxCapacity == 0) return 0.0;
        return (currentLoad * 100.0) / maxCapacity;
    }

    /**
     * Add inventory for a SKU
     */
    public void addInventory(String sku, int quantity) {
        if (inventory == null) {
            inventory = new HashMap<>();
        }
        inventory.merge(sku, quantity, Integer::sum);
    }

    /**
     * Remove inventory for a SKU
     */
    public boolean removeInventory(String sku, int quantity) {
        if (inventory == null || !inventory.containsKey(sku)) {
            return false;
        }
        int current = inventory.get(sku);
        if (current < quantity) {
            return false;
        }
        inventory.put(sku, current - quantity);
        return true;
    }

    /**
     * Get available quantity for a SKU
     */
    public int getAvailableQuantity(String sku) {
        if (inventory == null) return 0;
        return inventory.getOrDefault(sku, 0);
    }
}
