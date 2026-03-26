package com.iwos.darkstore.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;

@Entity @Table(name = "dark_stores")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class DarkStore {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private String id;
    @Column(nullable = false, unique = true, length = 20) private String storeCode;
    @Column(nullable = false) private String name;
    @Column(nullable = false) private String address;
    @Column(nullable = false) private String city;
    @Column(nullable = false) private String pincode;
    @Column(nullable = false) private Double latitude;
    @Column(nullable = false) private Double longitude;
    @Column(nullable = false) private Double serviceRadiusKm;   // Typical: 2-4 km
    @Column(nullable = false) @Builder.Default private Integer maxSkuCapacity = 2000;
    @Column(nullable = false) @Builder.Default private Integer currentSkuCount = 0;
    @Column(nullable = false) @Builder.Default private Integer maxDailyOrders = 500;
    @Column(nullable = false) @Builder.Default private Integer deliveryStaffCount = 0;
    @Enumerated(EnumType.STRING) @Builder.Default
    private StoreStatus status = StoreStatus.ACTIVE;
    private String parentWarehouseId;  // Main warehouse for replenishment
    @CreationTimestamp private Instant createdAt;

    public enum StoreStatus { ACTIVE, INACTIVE, MAINTENANCE, FULL }
}
