package com.iwos.shipmenthandoff.infrastructure.persistence.repository;

import com.iwos.shipmenthandoff.infrastructure.persistence.entity.ShipmentOutboxEventEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ShipmentOutboxEventRepository extends JpaRepository<ShipmentOutboxEventEntity, UUID> {

    @Query("SELECT e FROM ShipmentOutboxEventEntity e WHERE e.status = 'PENDING' ORDER BY e.createdAt ASC")
    List<ShipmentOutboxEventEntity> findPendingEvents(Pageable pageable);

    long countByStatus(String status);
}
