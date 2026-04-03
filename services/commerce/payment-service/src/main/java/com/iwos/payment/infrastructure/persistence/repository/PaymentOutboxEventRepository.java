package com.iwos.payment.infrastructure.persistence.repository;

import com.iwos.payment.infrastructure.persistence.entity.PaymentOutboxEventEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentOutboxEventRepository extends JpaRepository<PaymentOutboxEventEntity, UUID> {

    @Query("select e from PaymentOutboxEventEntity e where e.status = :status order by e.createdAt asc")
    List<PaymentOutboxEventEntity> findByStatusOrderByCreatedAtAsc(@Param("status") String status, Pageable pageable);

    long countByStatus(String status);

    @Modifying
    @Query("update PaymentOutboxEventEntity e set e.status = :status, e.publishedAt = CURRENT_TIMESTAMP where e.outboxEventId = :id")
    void markPublished(@Param("id") UUID outboxEventId, @Param("status") String status);

    @Modifying
    @Query("update PaymentOutboxEventEntity e set e.status = :status, e.attempts = e.attempts + 1, e.lastError = :error where e.outboxEventId = :id")
    void markFailed(@Param("id") UUID outboxEventId, @Param("status") String status, @Param("error") String error);
}
