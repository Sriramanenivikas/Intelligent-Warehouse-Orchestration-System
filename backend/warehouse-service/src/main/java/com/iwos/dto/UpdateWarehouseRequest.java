package com.iwos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Request DTO for updating an existing warehouse
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateWarehouseRequest {

    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;

    private String address;

    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @Size(max = 100, message = "State must not exceed 100 characters")
    private String state;

    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String country;

    @Size(max = 20, message = "Postal code must not exceed 20 characters")
    private String postalCode;

    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private Double latitude;

    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private Double longitude;

    @Positive(message = "Capacity in square meters must be positive")
    private BigDecimal capacitySqm;

    @Positive(message = "Max capacity must be positive")
    private Integer maxCapacity;

    @Min(value = 0, message = "Current load cannot be negative")
    private Integer currentLoad;

    @Min(value = 1, message = "Priority must be between 1 and 10")
    @Max(value = 10, message = "Priority must be between 1 and 10")
    private Integer priority;

    private Boolean isActive;

    // Contact information
    @Size(max = 100, message = "Manager name must not exceed 100 characters")
    private String managerName;

    @Size(max = 20, message = "Contact phone must not exceed 20 characters")
    private String contactPhone;

    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Contact email must not exceed 100 characters")
    private String contactEmail;

    @Size(max = 50, message = "Operating hours must not exceed 50 characters")
    private String operatingHours;
}
