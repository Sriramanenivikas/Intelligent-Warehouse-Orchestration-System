package com.iwos.catalog.repository;

import com.iwos.catalog.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {

    Optional<Product> findBySkuCode(String skuCode);

    Page<Product> findByCategoryIdAndActiveTrue(String categoryId, Pageable pageable);

    Page<Product> findByBrandIdAndActiveTrue(String brandId, Pageable pageable);

    Page<Product> findBySellerIdAndActiveTrue(String sellerId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.active = true AND p.featured = true ORDER BY p.avgRating DESC")
    List<Product> findFeaturedProducts(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.active = true AND LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Product> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    List<Product> findByIdIn(List<String> ids);

    boolean existsBySkuCode(String skuCode);
}
