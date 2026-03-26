package com.iwos.catalog.repository;

import com.iwos.catalog.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BrandRepository extends JpaRepository<Brand, String> {

    Optional<Brand> findBySlug(String slug);

    List<Brand> findByActiveTrueOrderByName();

    boolean existsByName(String name);
}
