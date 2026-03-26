package com.iwos.catalog.service;

import com.iwos.catalog.dto.request.CreateProductRequest;
import com.iwos.catalog.dto.request.UpdateProductRequest;
import com.iwos.catalog.dto.response.ProductResponse;
import com.iwos.catalog.entity.*;
import com.iwos.catalog.kafka.CatalogEventPublisher;
import com.iwos.catalog.mapper.ProductMapper;
import com.iwos.catalog.repository.BrandRepository;
import com.iwos.catalog.repository.CategoryRepository;
import com.iwos.catalog.repository.ProductRepository;
import com.iwos.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final SkuGeneratorService skuGenerator;
    private final CatalogEventPublisher eventPublisher;
    private final ProductMapper productMapper;

    public ProductResponse createProduct(CreateProductRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        Brand brand = null;
        if (request.getBrandId() != null) {
            brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", request.getBrandId()));
        }

        Product product = Product.builder()
                .skuCode(skuGenerator.generateSku(category.getSlug()))
                .name(request.getName())
                .description(request.getDescription())
                .mrp(request.getMrp())
                .sellingPrice(request.getSellingPrice())
                .discountPercentage(calculateDiscount(request.getMrp(), request.getSellingPrice()))
                .unit(request.getUnit())
                .weight(request.getWeight())
                .category(category)
                .brand(brand)
                .sellerId(request.getSellerId())
                .hsnCode(request.getHsnCode())
                .gstPercentage(request.getGstPercentage())
                .searchKeywords(request.getSearchKeywords())
                .build();

        if (request.getImageUrls() != null) {
            List<ProductImage> images = IntStream.range(0, request.getImageUrls().size())
                    .mapToObj(i -> ProductImage.builder()
                            .product(product)
                            .imageUrl(request.getImageUrls().get(i))
                            .sortOrder(i)
                            .isPrimary(i == 0)
                            .build())
                    .toList();
            product.getImages().addAll(images);
        }

        Product saved = productRepository.save(product);
        log.info("Product created: {} (SKU: {})", saved.getName(), saved.getSkuCode());

        eventPublisher.publishProductCreated(saved);
        return productMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return productMapper.toResponse(product);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductBySku(String skuCode) {
        Product product = productRepository.findBySkuCode(skuCode)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "skuCode", skuCode));
        return productMapper.toResponse(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByCategory(String categoryId, Pageable pageable) {
        return productRepository.findByCategoryIdAndActiveTrue(categoryId, pageable)
                .map(productMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsBySeller(String sellerId, Pageable pageable) {
        return productRepository.findBySellerIdAndActiveTrue(sellerId, pageable)
                .map(productMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(String keyword, Pageable pageable) {
        return productRepository.searchByKeyword(keyword, pageable)
                .map(productMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getFeaturedProducts(int limit) {
        return productRepository.findFeaturedProducts(Pageable.ofSize(limit))
                .stream().map(productMapper::toResponse).toList();
    }

    public ProductResponse updateProduct(String id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        if (request.getName() != null) product.setName(request.getName());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getMrp() != null) product.setMrp(request.getMrp());
        if (request.getSellingPrice() != null) product.setSellingPrice(request.getSellingPrice());
        if (request.getMrp() != null || request.getSellingPrice() != null) {
            product.setDiscountPercentage(calculateDiscount(
                    request.getMrp() != null ? request.getMrp() : product.getMrp(),
                    request.getSellingPrice() != null ? request.getSellingPrice() : product.getSellingPrice()));
        }
        if (request.getUnit() != null) product.setUnit(request.getUnit());
        if (request.getWeight() != null) product.setWeight(request.getWeight());
        if (request.getActive() != null) product.setActive(request.getActive());
        if (request.getFeatured() != null) product.setFeatured(request.getFeatured());
        if (request.getHsnCode() != null) product.setHsnCode(request.getHsnCode());
        if (request.getGstPercentage() != null) product.setGstPercentage(request.getGstPercentage());
        if (request.getSearchKeywords() != null) product.setSearchKeywords(request.getSearchKeywords());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
            product.setCategory(category);
        }

        Product saved = productRepository.save(product);
        eventPublisher.publishProductUpdated(saved);
        return productMapper.toResponse(saved);
    }

    public void deleteProduct(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        product.setActive(false);
        productRepository.save(product);
        eventPublisher.publishProductDeleted(id);
    }

    private BigDecimal calculateDiscount(BigDecimal mrp, BigDecimal sellingPrice) {
        if (mrp == null || sellingPrice == null || mrp.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return mrp.subtract(sellingPrice)
                .divide(mrp, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
