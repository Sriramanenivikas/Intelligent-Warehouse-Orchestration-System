package com.iwos.catalog.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(max = 255)
    private String name;

    private String description;

    @NotNull(message = "MRP is required")
    @DecimalMin(value = "0.01")
    private BigDecimal mrp;

    @NotNull(message = "Selling price is required")
    @DecimalMin(value = "0.01")
    private BigDecimal sellingPrice;

    private String unit;
    private BigDecimal weight;

    @NotBlank(message = "Category ID is required")
    private String categoryId;

    private String brandId;

    @NotBlank(message = "Seller ID is required")
    private String sellerId;

    private String hsnCode;
    private BigDecimal gstPercentage;
    private String searchKeywords;
    private List<String> imageUrls;
}
