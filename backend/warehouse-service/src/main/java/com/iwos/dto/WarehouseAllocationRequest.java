package com.iwos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

/**
 * Request DTO for warehouse allocation
 * Finds the optimal warehouse for order fulfillment
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseAllocationRequest {

    @NotNull(message = "Order ID is required")
    private String orderId;

    @NotNull(message = "Customer latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private Double customerLatitude;

    @NotNull(message = "Customer longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private Double customerLongitude;

    @NotNull(message = "Items list is required")
    @NotEmpty(message = "Items list cannot be empty")
    @Valid
    private List<OrderItemDTO> items;

    @NotBlank(message = "Delivery type is required")
    @Pattern(regexp = "^(EXPRESS|STANDARD)$", message = "Delivery type must be EXPRESS or STANDARD")
    private String deliveryType;

    private String customerAddress;
    private String customerCity;
    private String customerState;
    private String customerCountry;
}
