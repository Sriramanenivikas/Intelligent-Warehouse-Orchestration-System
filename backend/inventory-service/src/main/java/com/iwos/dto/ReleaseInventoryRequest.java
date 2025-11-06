package com.iwos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Request DTO for releasing reserved inventory
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReleaseInventoryRequest {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotNull(message = "Warehouse ID is required")
    private Long warehouseId;

    @NotNull(message = "Items list is required")
    private List<ReleaseItem> items;

    private String reason;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReleaseItem {

        @NotNull(message = "SKU ID is required")
        private Long skuId;

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;
    }
}
