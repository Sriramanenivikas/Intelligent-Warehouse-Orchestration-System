package com.iwos.serviceability.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "pincode_mappings", indexes = @Index(name = "idx_pincode", columnList = "pincode"))
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class PincodeMapping {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private String id;
    @Column(nullable = false, length = 6) private String pincode;
    @Column(nullable = false) private String city;
    @Column(nullable = false) private String state;
    private String zoneId;
    private String darkStoreId;
    private String warehouseId;
    @Column(nullable = false) @Builder.Default private boolean serviceable = true;
    @Column(nullable = false) @Builder.Default private Integer estimatedHours = 48;
}
