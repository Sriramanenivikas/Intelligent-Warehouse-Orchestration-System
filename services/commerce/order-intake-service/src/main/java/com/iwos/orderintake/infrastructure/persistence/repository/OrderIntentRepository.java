package com.iwos.orderintake.infrastructure.persistence.repository;

import com.iwos.orderintake.infrastructure.persistence.entity.OrderIntentEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderIntentRepository extends JpaRepository<OrderIntentEntity, UUID> {

    @EntityGraph(attributePaths = "items")
    Optional<OrderIntentEntity> findByOrderIntentId(UUID orderIntentId);
}
