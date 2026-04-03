package com.iwos.orderorchestrator.infrastructure.persistence.repository;

import com.iwos.orderorchestrator.infrastructure.persistence.entity.OrderOrchestratorOutboxEventEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderOrchestratorOutboxEventRepository extends JpaRepository<OrderOrchestratorOutboxEventEntity, UUID> {
}
