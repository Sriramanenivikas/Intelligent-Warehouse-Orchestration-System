package com.iwos.inventoryledger.infrastructure.persistence.repository;

import com.iwos.inventoryledger.infrastructure.persistence.entity.InventoryLedgerEntryEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryLedgerEntryRepository extends JpaRepository<InventoryLedgerEntryEntity, UUID> {
}
