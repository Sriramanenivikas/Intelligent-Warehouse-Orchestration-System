package com.iwos.seller.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity @Table(name = "seller_products", uniqueConstraints = @UniqueConstraint(columnNames = {"sellerId", "productId"}))
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class SellerProduct {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private String id;
    @Column(nullable = false) private String sellerId;
    @Column(nullable = false) private String productId;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal sellerPrice;
    @Column(nullable = false) @Builder.Default private Integer stockQuantity = 0;
    @Column(nullable = false) @Builder.Default private boolean active = true;
}
