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
public class InventoryEvent implements Serializable {
    private String eventId;
    private String skuCode;
    private String warehouseId;
    private String eventType;
    private Integer quantity;
    private Instant timestamp;
}
