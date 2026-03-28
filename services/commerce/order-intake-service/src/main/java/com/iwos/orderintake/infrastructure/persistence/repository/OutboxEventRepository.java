package com.iwos.orderintake.infrastructure.persistence.repository;

import com.iwos.orderintake.infrastructure.persistence.entity.OutboxEventEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxEventRepository extends JpaRepository<OutboxEventEntity, UUID> {
}
