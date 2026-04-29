package com.iwos.warehouseorchestrator.infrastructure.persistence.repository;

import com.iwos.warehouseorchestrator.infrastructure.persistence.entity.WarehouseOutboxEventEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface WarehouseOutboxEventRepository extends JpaRepository<WarehouseOutboxEventEntity, UUID> {

    List<WarehouseOutboxEventEntity> findByStatusOrderByCreatedAtAsc(String status);

    long countByStatus(String status);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
            update WarehouseOutboxEventEntity event
               set event.status = :status,
                   event.lastError = :lastError,
                   event.publishedAt = case when :status = 'PUBLISHED' then current_timestamp else event.publishedAt end,
                   event.attempts = event.attempts + 1
             where event.outboxEventId = :outboxEventId
            """)
    void updateStatus(
            @Param("outboxEventId") UUID outboxEventId,
            @Param("status") String status,
            @Param("lastError") String lastError
    );
}
