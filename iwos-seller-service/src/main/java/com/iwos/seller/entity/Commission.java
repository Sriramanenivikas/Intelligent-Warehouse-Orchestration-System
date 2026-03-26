package com.iwos.seller.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity @Table(name = "commissions")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Commission {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private String id;
    @Column(nullable = false) private String sellerId;
    @Column(nullable = false) private String orderId;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal orderAmount;
    @Column(nullable = false, precision = 5, scale = 2) private BigDecimal commissionRate;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal commissionAmount;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal netPayable;
    private Instant createdAt;
}
