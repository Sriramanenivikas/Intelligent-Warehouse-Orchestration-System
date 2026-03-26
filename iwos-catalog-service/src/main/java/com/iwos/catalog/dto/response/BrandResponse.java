package com.iwos.catalog.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BrandResponse {
    private String id;
    private String name;
    private String slug;
    private String logoUrl;
    private String description;
}
