package com.iwos.orderorchestrator.infrastructure.persistence.repository;

import com.iwos.orderorchestrator.infrastructure.persistence.entity.OrderWorkflowEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderWorkflowRepository extends JpaRepository<OrderWorkflowEntity, UUID> {

    @EntityGraph(attributePaths = "reservations")
    Optional<OrderWorkflowEntity> findByOrderIntentId(UUID orderIntentId);

    @EntityGraph(attributePaths = "reservations")
    Optional<OrderWorkflowEntity> findBySourceOutboxEventId(UUID sourceOutboxEventId);

    @EntityGraph(attributePaths = "reservations")
    @Query("select workflow from OrderWorkflowEntity workflow where workflow.workflowId = :workflowId")
    Optional<OrderWorkflowEntity> findDetailedByWorkflowId(UUID workflowId);
}
