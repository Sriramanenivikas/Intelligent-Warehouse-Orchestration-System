package com.iwos.serviceability.entity;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DeliveryPromise {
    private boolean serviceable;
    private String deliveryType;           // EXPRESS_10MIN, SAME_DAY, etc.
    private Integer estimatedMinutes;
    private String displayText;            // "Delivery in 10 minutes" / "Tomorrow by 9 PM"
    private String darkStoreId;
    private String warehouseId;
    private Double deliveryFee;
    private Double freeDeliveryAbove;
}
