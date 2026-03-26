package com.iwos.catalog.dto.request;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class UpdateProductRequest {
    private String name;
    private String description;
    private BigDecimal mrp;
    private BigDecimal sellingPrice;
    private String unit;
    private BigDecimal weight;
    private String categoryId;
    private String brandId;
    private String hsnCode;
    private BigDecimal gstPercentage;
    private String searchKeywords;
    private Boolean active;
    private Boolean featured;
    private List<String> imageUrls;
}
