package com.iwos.catalog.controller;

import com.iwos.catalog.dto.request.CreateProductRequest;
import com.iwos.catalog.dto.request.UpdateProductRequest;
import com.iwos.catalog.dto.response.ProductResponse;
import com.iwos.catalog.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable String id) {
        return ResponseEntity.ok(productService.getProduct(id));
    }

    @GetMapping("/sku/{skuCode}")
    public ResponseEntity<ProductResponse> getProductBySku(@PathVariable String skuCode) {
        return ResponseEntity.ok(productService.getProductBySku(skuCode));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<ProductResponse>> getByCategory(
            @PathVariable String categoryId, @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(productService.getProductsByCategory(categoryId, pageable));
    }

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<Page<ProductResponse>> getBySeller(
            @PathVariable String sellerId, @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(productService.getProductsBySeller(sellerId, pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponse>> search(
            @RequestParam String q, @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(productService.searchProducts(q, pageable));
    }

    @GetMapping("/featured")
    public ResponseEntity<List<ProductResponse>> getFeatured(@RequestParam(defaultValue = "12") int limit) {
        return ResponseEntity.ok(productService.getFeaturedProducts(limit));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable String id, @Valid @RequestBody UpdateProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
