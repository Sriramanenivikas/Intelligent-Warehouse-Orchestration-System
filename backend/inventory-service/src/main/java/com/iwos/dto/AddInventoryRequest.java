package com.iwos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for adding inventory
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddInventoryRequest {

    @NotNull(message = "SKU ID is required")
    private Long skuId;

    @NotNull(message = "Warehouse ID is required")
    private Long warehouseId;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity must be non-negative")
    private Integer quantity;

    private String notes;
}
