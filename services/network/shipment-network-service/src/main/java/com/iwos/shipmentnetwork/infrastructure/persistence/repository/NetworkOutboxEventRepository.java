package com.iwos.shipmentnetwork.infrastructure.persistence.repository;

import com.iwos.shipmentnetwork.infrastructure.persistence.entity.NetworkOutboxEventEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface NetworkOutboxEventRepository extends JpaRepository<NetworkOutboxEventEntity, UUID> {

    @Query("SELECT e FROM NetworkOutboxEventEntity e WHERE e.status = 'PENDING' ORDER BY e.createdAt ASC")
    List<NetworkOutboxEventEntity> findPendingEvents(Pageable pageable);

    long countByStatus(String status);
}
