package com.iwos.catalog.repository;

import com.iwos.catalog.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {

    Optional<Category> findBySlug(String slug);

    List<Category> findByParentIsNullAndActiveTrueOrderBySortOrder();

    List<Category> findByParentIdAndActiveTrueOrderBySortOrder(String parentId);

    @Query("SELECT c FROM Category c WHERE c.level = :level AND c.active = true ORDER BY c.sortOrder")
    List<Category> findByLevel(Integer level);

    boolean existsBySlug(String slug);
}
