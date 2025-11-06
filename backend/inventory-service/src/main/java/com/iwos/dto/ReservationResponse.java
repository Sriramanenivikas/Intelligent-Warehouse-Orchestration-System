package com.iwos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for inventory reservation operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponse {

    private Boolean success;
    private String message;
    private Long orderId;
    private List<ReservedItem> reservedItems;
    private List<String> errors;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReservedItem {
        private Long skuId;
        private String skuCode;
        private Integer quantityReserved;
        private Integer quantityAvailable;
    }
}
