package com.iwos.serviceability.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "service_zones")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ServiceZone {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private String id;
    @Column(nullable = false) private String zoneName;
    @Column(nullable = false) private String city;
    @Column(nullable = false) private Double centerLat;
    @Column(nullable = false) private Double centerLng;
    @Column(nullable = false) private Double radiusKm;
    @Column(nullable = false) private String darkStoreId;
    @Column(nullable = false) private String warehouseId;
    @Enumerated(EnumType.STRING) @Builder.Default
    private DeliveryType deliveryType = DeliveryType.STANDARD;
    @Column(nullable = false) @Builder.Default private boolean active = true;

    public enum DeliveryType { EXPRESS_10MIN, EXPRESS_30MIN, SAME_DAY, NEXT_DAY, STANDARD }
}
