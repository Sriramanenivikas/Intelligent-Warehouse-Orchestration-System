package com.iwos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private String orderId;
    private String customerId;
    private String customerName;
    private String status;
    private String warehouseId;
    private String warehouseName;
    private BigDecimal totalAmount;
    private String deliveryType;
    private String paymentMethod;
    private DeliveryAddressDTO deliveryAddress;
    private List<OrderItemDTO> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Double distanceKm;
    private Integer estimatedDeliveryMinutes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDTO {
        private String sku;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveryAddressDTO {
        private String line1;
        private String line2;
        private String city;
        private String state;
        private String pincode;
        private Double latitude;
        private Double longitude;
    }
}
