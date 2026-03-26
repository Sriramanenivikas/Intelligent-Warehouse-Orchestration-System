package com.iwos.returns.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity @Table(name = "return_items")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ReturnItem {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private String id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "return_request_id") private ReturnRequest returnRequest;
    @Column(nullable = false) private String productId;
    @Column(nullable = false) private String skuCode;
    @Column(nullable = false) private Integer quantity;
    @Column(precision = 12, scale = 2) private BigDecimal unitPrice;
}
