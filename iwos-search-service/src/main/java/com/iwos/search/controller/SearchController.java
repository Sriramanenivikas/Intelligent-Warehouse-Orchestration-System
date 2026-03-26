package com.iwos.search.controller;

import com.iwos.search.model.ProductDocument;
import com.iwos.search.service.AutocompleteService;
import com.iwos.search.service.ProductSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final ProductSearchService searchService;
    private final AutocompleteService autocompleteService;

    @GetMapping
    public ResponseEntity<List<ProductDocument>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String brandId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "relevance") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(searchService.search(q, categoryId, brandId, minPrice, maxPrice, sortBy, page, size));
    }

    @GetMapping("/autocomplete")
    public ResponseEntity<List<String>> autocomplete(
            @RequestParam String q, @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(autocompleteService.suggest(q, limit));
    }
}
