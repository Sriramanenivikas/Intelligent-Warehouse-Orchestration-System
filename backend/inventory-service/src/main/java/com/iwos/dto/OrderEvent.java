package com.iwos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Order Event DTO for Kafka messages
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {

    private String eventType;
    private String eventId;
    private LocalDateTime timestamp;
    private OrderData order;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderData {
        private Long orderId;
        private String orderNumber;
        private Long customerId;
        private Long warehouseId;
        private String status;
        private BigDecimal totalAmount;
        private List<OrderItem> items;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem {
        private Long skuId;
        private String skuCode;
        private Integer quantity;
        private BigDecimal unitPrice;
    }
}
