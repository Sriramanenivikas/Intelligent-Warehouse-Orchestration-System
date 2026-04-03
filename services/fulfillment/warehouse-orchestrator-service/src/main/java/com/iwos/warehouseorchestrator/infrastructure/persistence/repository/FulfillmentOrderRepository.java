package com.iwos.warehouseorchestrator.infrastructure.persistence.repository;

import com.iwos.warehouseorchestrator.domain.fulfillment.FulfillmentOrderStatus;
import com.iwos.warehouseorchestrator.infrastructure.persistence.entity.FulfillmentOrderEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FulfillmentOrderRepository extends JpaRepository<FulfillmentOrderEntity, UUID> {

    long countByStatus(FulfillmentOrderStatus status);

    @EntityGraph(attributePaths = "tasks")
    Optional<FulfillmentOrderEntity> findByOrderIntentId(UUID orderIntentId);

    @EntityGraph(attributePaths = "tasks")
    Optional<FulfillmentOrderEntity> findByWorkflowId(UUID workflowId);

    Optional<FulfillmentOrderEntity> findBySourceMessageKey(String sourceMessageKey);

    @EntityGraph(attributePaths = "tasks")
    @Query("select fulfillmentOrder from FulfillmentOrderEntity fulfillmentOrder where fulfillmentOrder.fulfillmentOrderId = :fulfillmentOrderId")
    Optional<FulfillmentOrderEntity> findDetailedByFulfillmentOrderId(@Param("fulfillmentOrderId") UUID fulfillmentOrderId);

    List<FulfillmentOrderEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<FulfillmentOrderEntity> findAllByStatusOrderByCreatedAtDesc(FulfillmentOrderStatus status, Pageable pageable);
}
