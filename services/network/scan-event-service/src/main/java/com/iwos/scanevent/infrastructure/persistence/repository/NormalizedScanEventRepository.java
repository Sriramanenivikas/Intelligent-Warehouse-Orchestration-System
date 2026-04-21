package com.iwos.scanevent.infrastructure.persistence.repository;

import com.iwos.scanevent.infrastructure.persistence.entity.NormalizedScanEventEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NormalizedScanEventRepository extends JpaRepository<NormalizedScanEventEntity, UUID> {

    boolean existsByScanEventId(UUID scanEventId);

    Optional<NormalizedScanEventEntity> findByScanEventId(UUID scanEventId);

    List<NormalizedScanEventEntity> findByShipmentIdOrderByOccurredAtAsc(UUID shipmentId);
}
