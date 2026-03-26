package com.iwos.catalog.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class CategoryResponse {
    private String id;
    private String name;
    private String slug;
    private String description;
    private String imageUrl;
    private Integer level;
    private String parentId;
    private List<CategoryResponse> children;
}
