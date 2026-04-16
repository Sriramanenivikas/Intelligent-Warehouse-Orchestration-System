package com.iwos.warehouseorchestrator.infrastructure.persistence.repository;

import com.iwos.warehouseorchestrator.domain.fulfillment.FulfillmentTaskStatus;
import com.iwos.warehouseorchestrator.domain.fulfillment.FulfillmentTaskType;
import com.iwos.warehouseorchestrator.infrastructure.persistence.entity.FulfillmentTaskEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FulfillmentTaskRepository extends JpaRepository<FulfillmentTaskEntity, UUID> {

    long countByStatus(FulfillmentTaskStatus status);

    Optional<FulfillmentTaskEntity> findByFulfillmentTaskId(UUID fulfillmentTaskId);

    List<FulfillmentTaskEntity> findByFulfillmentOrder_FulfillmentOrderId(UUID fulfillmentOrderId);

    long countByFulfillmentOrder_FulfillmentOrderIdAndStatusNot(UUID fulfillmentOrderId, FulfillmentTaskStatus status);

    long countByFulfillmentOrder_FulfillmentOrderIdAndTaskTypeAndStatus(
            UUID fulfillmentOrderId, FulfillmentTaskType taskType, FulfillmentTaskStatus status);
}
