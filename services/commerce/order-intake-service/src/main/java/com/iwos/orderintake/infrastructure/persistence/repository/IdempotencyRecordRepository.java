package com.iwos.orderintake.infrastructure.persistence.repository;

import com.iwos.orderintake.infrastructure.persistence.entity.IdempotencyRecordEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecordEntity, UUID> {

    Optional<IdempotencyRecordEntity> findByIdempotencyKey(String idempotencyKey);
}
