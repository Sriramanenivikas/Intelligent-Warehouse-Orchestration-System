package com.iwos.scanevent.infrastructure.persistence.repository;

import com.iwos.scanevent.infrastructure.persistence.entity.TrackedShipmentEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackedShipmentRepository extends JpaRepository<TrackedShipmentEntity, UUID> {

    Optional<TrackedShipmentEntity> findByShipmentId(UUID shipmentId);

    Optional<TrackedShipmentEntity> findByOrderIntentId(UUID orderIntentId);

    Optional<TrackedShipmentEntity> findByAwbNumberIgnoreCase(String awbNumber);

    long countByCurrentStatus(Enum<?> currentStatus);
}
