package com.iwos.catalog.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "product_variants")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, unique = true, length = 60)
    private String variantSkuCode;

    @Column(length = 50)
    private String size;

    @Column(length = 50)
    private String color;

    @Column(precision = 8, scale = 3)
    private BigDecimal variantWeight;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
