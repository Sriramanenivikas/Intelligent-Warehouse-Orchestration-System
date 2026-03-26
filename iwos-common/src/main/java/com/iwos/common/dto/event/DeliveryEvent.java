package com.iwos.common.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryEvent implements Serializable {
    private String eventId;
    private String deliveryId;
    private String orderId;
    private String eventType;
    private Double latitude;
    private Double longitude;
    private Instant timestamp;
}
