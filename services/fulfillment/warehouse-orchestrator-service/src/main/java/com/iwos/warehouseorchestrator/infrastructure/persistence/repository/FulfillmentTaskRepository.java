package com.iwos.warehouseorchestrator.infrastructure.persistence.repository;

import com.iwos.warehouseorchestrator.domain.fulfillment.FulfillmentTaskStatus;
import com.iwos.warehouseorchestrator.infrastructure.persistence.entity.FulfillmentTaskEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FulfillmentTaskRepository extends JpaRepository<FulfillmentTaskEntity, UUID> {

    long countByStatus(FulfillmentTaskStatus status);
}

