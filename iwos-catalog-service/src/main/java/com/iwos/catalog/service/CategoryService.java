package com.iwos.catalog.service;

import com.iwos.catalog.dto.request.CreateCategoryRequest;
import com.iwos.catalog.dto.response.CategoryResponse;
import com.iwos.catalog.entity.Category;
import com.iwos.catalog.repository.CategoryRepository;
import com.iwos.common.exception.BusinessRuleException;
import com.iwos.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryResponse createCategory(CreateCategoryRequest request) {
        if (categoryRepository.existsBySlug(generateSlug(request.getName()))) {
            throw new BusinessRuleException("Category with this name already exists");
        }

        Category parent = null;
        int level = 0;
        if (request.getParentId() != null) {
            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getParentId()));
            level = parent.getLevel() + 1;
        }

        Category category = Category.builder()
                .name(request.getName())
                .slug(generateSlug(request.getName()))
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .parent(parent)
                .level(level)
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .build();

        Category saved = categoryRepository.save(category);
        log.info("Category created: {} (level: {})", saved.getName(), saved.getLevel());
        return mapToResponse(saved, true);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getRootCategories() {
        return categoryRepository.findByParentIsNullAndActiveTrueOrderBySortOrder()
                .stream().map(c -> mapToResponse(c, true)).toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategory(String id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        return mapToResponse(category, true);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getSubcategories(String parentId) {
        return categoryRepository.findByParentIdAndActiveTrueOrderBySortOrder(parentId)
                .stream().map(c -> mapToResponse(c, false)).toList();
    }

    private CategoryResponse mapToResponse(Category category, boolean includeChildren) {
        CategoryResponse.CategoryResponseBuilder builder = CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .imageUrl(category.getImageUrl())
                .level(category.getLevel())
                .parentId(category.getParent() != null ? category.getParent().getId() : null);

        if (includeChildren && category.getChildren() != null) {
            builder.children(category.getChildren().stream()
                    .filter(Category::isActive)
                    .map(c -> mapToResponse(c, true)).toList());
        }
        return builder.build();
    }

    private String generateSlug(String name) {
        return name.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
    }
}
