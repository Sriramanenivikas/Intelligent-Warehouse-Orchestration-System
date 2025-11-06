package com.iwos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for inventory data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {

    private Long id;
    private Long skuId;
    private String skuCode;
    private String skuName;
    private Long warehouseId;
    private Integer quantityOnHand;
    private Integer quantityReserved;
    private Integer quantityAvailable;
    private LocalDateTime lastCountedAt;
}
