package com.iwos.notification.infrastructure.persistence.repository;

import com.iwos.notification.infrastructure.persistence.entity.NotificationOutboxEventEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface NotificationOutboxEventRepository extends JpaRepository<NotificationOutboxEventEntity, UUID> {

    @Query("SELECT e FROM NotificationOutboxEventEntity e WHERE e.status = 'PENDING' ORDER BY e.createdAt ASC")
    List<NotificationOutboxEventEntity> findPendingEvents(Pageable pageable);

    long countByStatus(String status);
}
