package com.iwos.darkstore.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.Instant;

@Entity @Table(name = "dark_store_stock",
    uniqueConstraints = @UniqueConstraint(columnNames = {"storeId", "skuCode"}),
    indexes = @Index(name = "idx_dss_store_sku", columnList = "storeId, skuCode"))
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class DarkStoreStock {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private String id;
    @Column(nullable = false) private String storeId;
    @Column(nullable = false, length = 50) private String skuCode;
    @Column(nullable = false) private String productId;
    @Column(nullable = false) @Builder.Default private Integer quantity = 0;
    @Column(nullable = false) @Builder.Default private Integer reservedQuantity = 0;
    @Column(nullable = false) @Builder.Default private Integer reorderLevel = 5;
    @Column(nullable = false) @Builder.Default private Integer maxLevel = 50;
    @Builder.Default private Integer dailySalesAvg = 0;
    @UpdateTimestamp private Instant updatedAt;

    public int getAvailableQuantity() {
        return quantity - reservedQuantity;
    }

    public boolean needsReplenishment() {
        return getAvailableQuantity() <= reorderLevel;
    }
}
