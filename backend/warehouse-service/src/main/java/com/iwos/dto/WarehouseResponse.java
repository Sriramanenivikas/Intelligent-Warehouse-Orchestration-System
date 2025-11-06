package com.iwos.dto;

import com.iwos.entity.Warehouse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Response DTO for warehouse data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseResponse {

    private Long id;
    private String code;
    private String name;
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private Double latitude;
    private Double longitude;
    private BigDecimal capacitySqm;
    private Integer maxCapacity;
    private Integer currentLoad;
    private Double loadPercentage;
    private Integer priority;
    private Boolean isActive;
    private Map<String, Integer> inventory;
    private String managerName;
    private String contactPhone;
    private String contactEmail;
    private String operatingHours;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Optional fields for allocation responses
    private Double distanceFromCustomer;
    private String distanceUnit;

    /**
     * Convert Warehouse entity to WarehouseResponse DTO
     */
    public static WarehouseResponse fromEntity(Warehouse warehouse) {
        if (warehouse == null) {
            return null;
        }

        return WarehouseResponse.builder()
            .id(warehouse.getId())
            .code(warehouse.getCode())
            .name(warehouse.getName())
            .address(warehouse.getAddress())
            .city(warehouse.getCity())
            .state(warehouse.getState())
            .country(warehouse.getCountry())
            .postalCode(warehouse.getPostalCode())
            .latitude(warehouse.getLatitude())
            .longitude(warehouse.getLongitude())
            .capacitySqm(warehouse.getCapacitySqm())
            .maxCapacity(warehouse.getMaxCapacity())
            .currentLoad(warehouse.getCurrentLoad())
            .loadPercentage(warehouse.getLoadPercentage())
            .priority(warehouse.getPriority())
            .isActive(warehouse.getIsActive())
            .inventory(warehouse.getInventory())
            .managerName(warehouse.getManagerName())
            .contactPhone(warehouse.getContactPhone())
            .contactEmail(warehouse.getContactEmail())
            .operatingHours(warehouse.getOperatingHours())
            .createdAt(warehouse.getCreatedAt())
            .updatedAt(warehouse.getUpdatedAt())
            .distanceFromCustomer(warehouse.getDistanceFromCustomer())
            .distanceUnit(warehouse.getDistanceFromCustomer() != null ? "km" : null)
            .build();
    }
}
