package com.iwos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * DTO for order items in allocation requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {

    @NotBlank(message = "SKU is required")
    private String sku;

    @Positive(message = "Quantity must be positive")
    private Integer quantity;

    private String productName;

    private Double weight;

    private Double volume;
}
