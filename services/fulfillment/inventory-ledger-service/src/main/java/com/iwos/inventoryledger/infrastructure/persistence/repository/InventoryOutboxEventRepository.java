package com.iwos.inventoryledger.infrastructure.persistence.repository;

import com.iwos.inventoryledger.infrastructure.persistence.entity.InventoryOutboxEventEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryOutboxEventRepository extends JpaRepository<InventoryOutboxEventEntity, UUID> {
}
