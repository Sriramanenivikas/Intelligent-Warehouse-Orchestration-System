package com.iwos.search.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDocument {
    private String id;
    private String skuCode;
    private String name;
    private String description;
    private BigDecimal mrp;
    private BigDecimal sellingPrice;
    private BigDecimal discountPercentage;
    private String categoryId;
    private String categoryName;
    private String brandId;
    private String brandName;
    private String sellerId;
    private String imageUrl;
    private BigDecimal avgRating;
    private Integer reviewCount;
    private String searchKeywords;
    private boolean active;
    private boolean featured;
}
