package com.iwos.darkstore.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;

@Entity @Table(name = "replenishment_orders")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ReplenishmentOrder {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private String id;
    @Column(nullable = false) private String storeId;
    @Column(nullable = false) private String skuCode;
    @Column(nullable = false) private Integer requestedQuantity;
    @Column(nullable = false) @Builder.Default private Integer fulfilledQuantity = 0;
    @Enumerated(EnumType.STRING) @Builder.Default
    private ReplenishmentStatus status = ReplenishmentStatus.PENDING;
    private String sourceWarehouseId;
    @CreationTimestamp private Instant createdAt;
    private Instant fulfilledAt;

    public enum ReplenishmentStatus { PENDING, IN_TRANSIT, PARTIALLY_FULFILLED, FULFILLED, CANCELLED }
}
