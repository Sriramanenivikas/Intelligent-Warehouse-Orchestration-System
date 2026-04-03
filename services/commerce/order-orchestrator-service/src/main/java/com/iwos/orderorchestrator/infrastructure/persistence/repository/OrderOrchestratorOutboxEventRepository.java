package com.iwos.orderorchestrator.infrastructure.persistence.repository;

import com.iwos.orderorchestrator.infrastructure.persistence.entity.OrderOrchestratorOutboxEventEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface OrderOrchestratorOutboxEventRepository extends JpaRepository<OrderOrchestratorOutboxEventEntity, UUID> {

    List<OrderOrchestratorOutboxEventEntity> findByStatusOrderByCreatedAtAsc(String status);

    long countByStatus(String status);

    @Modifying
    @Transactional
    @Query("UPDATE OrderOrchestratorOutboxEventEntity e SET e.status = :status WHERE e.outboxEventId = :id")
    void updateStatus(@Param("id") UUID outboxEventId, @Param("status") String status);
}
