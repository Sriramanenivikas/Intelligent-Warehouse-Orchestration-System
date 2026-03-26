package com.iwos.catalog.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateCategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(max = 100)
    private String name;

    private String description;
    private String imageUrl;
    private String parentId;
    private Integer sortOrder;
}
