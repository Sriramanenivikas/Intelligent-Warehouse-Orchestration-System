package com.iwos.warehouse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Warehouse Domain Model
 * Represents a warehouse in the allocation algorithm
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Warehouse {

    private String id;
    private String name;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private Double latitude;
    private Double longitude;

    // Calculated during allocation
    private Double distanceFromCustomer;  // in kilometers
    private Integer estimatedDeliveryMinutes;
    private Boolean hasAllItems;
    private Integer inventoryScore;  // How well stocked this warehouse is

    /**
     * Calculate score for warehouse allocation
     * Lower score is better (closer distance, better inventory)
     */
    public double calculateAllocationScore() {
        double distanceWeight = 0.7;
        double inventoryWeight = 0.3;

        double normalizedDistance = distanceFromCustomer / 100.0;  // Normalize to 0-1 range
        double normalizedInventory = hasAllItems ? 0.0 : 1.0;

        return (distanceWeight * normalizedDistance) + (inventoryWeight * normalizedInventory);
    }
}
