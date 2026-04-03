package com.iwos.payment.infrastructure.persistence.repository;

import com.iwos.payment.infrastructure.persistence.entity.PaymentIdempotencyRecordEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentIdempotencyRecordRepository extends JpaRepository<PaymentIdempotencyRecordEntity, java.util.UUID> {

    Optional<PaymentIdempotencyRecordEntity> findByIdempotencyKeyAndOperationType(String idempotencyKey, String operationType);
}
