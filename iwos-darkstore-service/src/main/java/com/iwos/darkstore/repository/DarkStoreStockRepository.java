package com.iwos.darkstore.repository;

import com.iwos.darkstore.entity.DarkStoreStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DarkStoreStockRepository extends JpaRepository<DarkStoreStock, String> {
    Optional<DarkStoreStock> findByStoreIdAndSkuCode(String storeId, String skuCode);
    List<DarkStoreStock> findByStoreId(String storeId);

    @Query("SELECT s FROM DarkStoreStock s WHERE s.storeId = :storeId AND (s.quantity - s.reservedQuantity) <= s.reorderLevel")
    List<DarkStoreStock> findLowStockItems(String storeId);
}
