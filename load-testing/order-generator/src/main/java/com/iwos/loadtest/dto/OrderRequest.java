package com.iwos.loadtest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Order Request DTO - matches the Order Service API contract
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {

    private String customerId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private List<OrderItemDTO> items;
    private DeliveryAddressDTO deliveryAddress;
    private String deliveryType;
    private String paymentMethod;
    private BigDecimal totalAmount;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDTO {
        private String sku;
        private Integer quantity;
        private BigDecimal unitPrice;
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
