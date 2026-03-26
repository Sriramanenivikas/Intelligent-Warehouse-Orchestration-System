package com.iwos.seller.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity @Table(name = "settlements")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Settlement {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private String id;
    @Column(nullable = false) private String sellerId;
    @Column(nullable = false) private LocalDate periodStart;
    @Column(nullable = false) private LocalDate periodEnd;
    @Column(nullable = false, precision = 14, scale = 2) private BigDecimal totalSales;
    @Column(nullable = false, precision = 14, scale = 2) private BigDecimal totalCommission;
    @Column(nullable = false, precision = 14, scale = 2) private BigDecimal totalDeductions;
    @Column(nullable = false, precision = 14, scale = 2) private BigDecimal netPayable;
    @Enumerated(EnumType.STRING) @Builder.Default private SettlementStatus status = SettlementStatus.PENDING;
    private String utrNumber;
    private Instant paidAt;
    private Instant createdAt;

    public enum SettlementStatus { PENDING, PROCESSING, PAID, FAILED }
}
