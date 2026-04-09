package com.iwos.payment.infrastructure.persistence.repository;

import com.iwos.payment.infrastructure.persistence.entity.PaymentIntentEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentIntentRepository extends JpaRepository<PaymentIntentEntity, UUID> {

    Optional<PaymentIntentEntity> findByPaymentIntentId(UUID paymentIntentId);

    Optional<PaymentIntentEntity> findByOrderIntentId(UUID orderIntentId);

    long countByStatus(String status);
}
