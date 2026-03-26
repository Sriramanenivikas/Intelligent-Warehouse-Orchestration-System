package com.iwos.catalog.mapper;

import com.iwos.catalog.dto.response.BrandResponse;
import com.iwos.catalog.dto.response.CategoryResponse;
import com.iwos.catalog.dto.response.ProductResponse;
import com.iwos.catalog.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .skuCode(product.getSkuCode())
                .name(product.getName())
                .description(product.getDescription())
                .mrp(product.getMrp())
                .sellingPrice(product.getSellingPrice())
                .discountPercentage(product.getDiscountPercentage())
                .unit(product.getUnit())
                .weight(product.getWeight())
                .category(product.getCategory() != null ? CategoryResponse.builder()
                        .id(product.getCategory().getId())
                        .name(product.getCategory().getName())
                        .slug(product.getCategory().getSlug())
                        .build() : null)
                .brand(product.getBrand() != null ? BrandResponse.builder()
                        .id(product.getBrand().getId())
                        .name(product.getBrand().getName())
                        .slug(product.getBrand().getSlug())
                        .build() : null)
                .sellerId(product.getSellerId())
                .imageUrls(product.getImages().stream().map(i -> i.getImageUrl()).toList())
                .active(product.isActive())
                .featured(product.isFeatured())
                .avgRating(product.getAvgRating())
                .reviewCount(product.getReviewCount())
                .createdAt(product.getCreatedAt())
                .build();
    }
}
