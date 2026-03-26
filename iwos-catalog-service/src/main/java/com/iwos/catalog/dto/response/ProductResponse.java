package com.iwos.catalog.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class ProductResponse {
    private String id;
    private String skuCode;
    private String name;
    private String description;
    private BigDecimal mrp;
    private BigDecimal sellingPrice;
    private BigDecimal discountPercentage;
    private String unit;
    private BigDecimal weight;
    private CategoryResponse category;
    private BrandResponse brand;
    private String sellerId;
    private List<String> imageUrls;
    private boolean active;
    private boolean featured;
    private BigDecimal avgRating;
    private Integer reviewCount;
    private Instant createdAt;
}
