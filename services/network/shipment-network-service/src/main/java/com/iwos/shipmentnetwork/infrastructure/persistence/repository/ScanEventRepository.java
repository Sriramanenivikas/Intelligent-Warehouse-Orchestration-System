package com.iwos.shipmentnetwork.infrastructure.persistence.repository;

import com.iwos.shipmentnetwork.infrastructure.persistence.entity.ScanEventEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScanEventRepository extends JpaRepository<ScanEventEntity, UUID> {

    List<ScanEventEntity> findByShipmentIdOrderByOccurredAtAsc(UUID shipmentId);

    long countByShipmentId(UUID shipmentId);
}
