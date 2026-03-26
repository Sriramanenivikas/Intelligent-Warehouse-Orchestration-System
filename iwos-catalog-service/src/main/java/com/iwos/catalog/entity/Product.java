package com.iwos.catalog.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_product_sku", columnList = "skuCode", unique = true),
    @Index(name = "idx_product_category", columnList = "category_id"),
    @Index(name = "idx_product_brand", columnList = "brand_id"),
    @Index(name = "idx_product_seller", columnList = "sellerId"),
    @Index(name = "idx_product_active", columnList = "active")
})
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true, length = 50)
    private String skuCode;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal mrp;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal sellingPrice;

    @Column(precision = 5, scale = 2)
    private BigDecimal discountPercentage;

    @Column(length = 20)
    private String unit;           // kg, litre, piece, pack

    @Column(precision = 8, scale = 3)
    private BigDecimal weight;     // in grams

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @Column(nullable = false)
    private String sellerId;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductVariant> variants = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean featured = false;

    private String hsnCode;        // GST harmonized code

    @Column(precision = 5, scale = 2)
    private BigDecimal gstPercentage;

    @Column(length = 500)
    private String searchKeywords;

    @Column(precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal avgRating = BigDecimal.ZERO;

    @Builder.Default
    private Integer reviewCount = 0;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    @Version
    private Long version;
}
