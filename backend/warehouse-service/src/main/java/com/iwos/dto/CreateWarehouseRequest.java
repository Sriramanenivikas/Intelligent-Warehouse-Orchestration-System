package com.iwos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Request DTO for creating a new warehouse
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateWarehouseRequest {

    @NotBlank(message = "Warehouse code is required")
    @Size(max = 50, message = "Code must not exceed 50 characters")
    @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Code must contain only uppercase letters, numbers, hyphens, and underscores")
    private String code;

    @NotBlank(message = "Warehouse name is required")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @Size(max = 100, message = "State must not exceed 100 characters")
    private String state;

    @NotBlank(message = "Country is required")
    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String country;

    @Size(max = 20, message = "Postal code must not exceed 20 characters")
    private String postalCode;

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private Double longitude;

    @Positive(message = "Capacity in square meters must be positive")
    private BigDecimal capacitySqm;

    @NotNull(message = "Max capacity is required")
    @Positive(message = "Max capacity must be positive")
    private Integer maxCapacity;

    @Min(value = 1, message = "Priority must be between 1 and 10")
    @Max(value = 10, message = "Priority must be between 1 and 10")
    private Integer priority;

    @Builder.Default
    private Boolean isActive = true;

    // Initial inventory (optional)
    @Builder.Default
    private Map<String, Integer> inventory = new HashMap<>();

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
