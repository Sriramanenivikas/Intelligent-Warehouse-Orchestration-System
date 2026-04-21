package com.iwos.notification.infrastructure.persistence.repository;

import com.iwos.notification.domain.notification.NotificationAudience;
import com.iwos.notification.domain.notification.NotificationStatus;
import com.iwos.notification.infrastructure.persistence.entity.NotificationEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<NotificationEntity, UUID> {

    List<NotificationEntity> findByOrderIntentIdOrderByCreatedAtDesc(UUID orderIntentId);

    List<NotificationEntity> findByShipmentIdOrderByCreatedAtDesc(UUID shipmentId);

    List<NotificationEntity> findByAudienceOrderByCreatedAtDesc(NotificationAudience audience);

    long countByStatus(NotificationStatus status);

    long countByAudience(NotificationAudience audience);
}
