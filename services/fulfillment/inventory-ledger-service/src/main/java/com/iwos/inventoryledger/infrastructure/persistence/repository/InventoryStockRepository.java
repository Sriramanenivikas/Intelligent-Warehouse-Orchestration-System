package com.iwos.inventoryledger.infrastructure.persistence.repository;

import com.iwos.inventoryledger.infrastructure.persistence.entity.InventoryStockEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryStockRepository extends JpaRepository<InventoryStockEntity, UUID> {

    Optional<InventoryStockEntity> findByNodeIdAndSku(String nodeId, String sku);
}
