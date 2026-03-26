package com.iwos.catalog.controller;

import com.iwos.catalog.dto.request.CreateCategoryRequest;
import com.iwos.catalog.dto.response.CategoryResponse;
import com.iwos.catalog.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CreateCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(request));
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getRootCategories() {
        return ResponseEntity.ok(categoryService.getRootCategories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategory(@PathVariable String id) {
        return ResponseEntity.ok(categoryService.getCategory(id));
    }

    @GetMapping("/{id}/subcategories")
    public ResponseEntity<List<CategoryResponse>> getSubcategories(@PathVariable String id) {
        return ResponseEntity.ok(categoryService.getSubcategories(id));
    }
}
