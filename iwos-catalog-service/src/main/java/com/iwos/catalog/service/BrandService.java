package com.iwos.catalog.service;

import com.iwos.catalog.dto.response.BrandResponse;
import com.iwos.catalog.entity.Brand;
import com.iwos.catalog.repository.BrandRepository;
import com.iwos.common.exception.BusinessRuleException;
import com.iwos.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BrandService {

    private final BrandRepository brandRepository;

    public BrandResponse createBrand(String name, String logoUrl, String description) {
        if (brandRepository.existsByName(name)) {
            throw new BusinessRuleException("Brand already exists: " + name);
        }
        Brand brand = Brand.builder()
                .name(name)
                .slug(name.toLowerCase().replaceAll("[^a-z0-9]+", "-"))
                .logoUrl(logoUrl)
                .description(description)
                .build();
        Brand saved = brandRepository.save(brand);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<BrandResponse> getAllBrands() {
        return brandRepository.findByActiveTrueOrderByName().stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public BrandResponse getBrand(String id) {
        return mapToResponse(brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", id)));
    }

    private BrandResponse mapToResponse(Brand b) {
        return BrandResponse.builder().id(b.getId()).name(b.getName()).slug(b.getSlug())
                .logoUrl(b.getLogoUrl()).description(b.getDescription()).build();
    }
}
