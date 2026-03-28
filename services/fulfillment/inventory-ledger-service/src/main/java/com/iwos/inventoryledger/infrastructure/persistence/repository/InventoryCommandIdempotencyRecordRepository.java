package com.iwos.inventoryledger.infrastructure.persistence.repository;

import com.iwos.inventoryledger.infrastructure.persistence.entity.InventoryCommandIdempotencyRecordEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryCommandIdempotencyRecordRepository extends JpaRepository<InventoryCommandIdempotencyRecordEntity, UUID> {

    Optional<InventoryCommandIdempotencyRecordEntity> findByIdempotencyKey(String idempotencyKey);
}
