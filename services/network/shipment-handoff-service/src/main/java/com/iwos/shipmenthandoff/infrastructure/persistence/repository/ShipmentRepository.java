package com.iwos.shipmenthandoff.infrastructure.persistence.repository;

import com.iwos.shipmenthandoff.domain.shipment.ShipmentStatus;
import com.iwos.shipmenthandoff.infrastructure.persistence.entity.ShipmentEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShipmentRepository extends JpaRepository<ShipmentEntity, UUID> {

    Optional<ShipmentEntity> findByFulfillmentOrderId(UUID fulfillmentOrderId);

    Optional<ShipmentEntity> findByOrderIntentId(UUID orderIntentId);

    Optional<ShipmentEntity> findByAwbNumber(String awbNumber);

    List<ShipmentEntity> findByStatusOrderByCreatedAtDesc(ShipmentStatus status, Pageable pageable);

    List<ShipmentEntity> findByOriginNodeIdAndStatusOrderByCreatedAtDesc(String originNodeId, ShipmentStatus status, Pageable pageable);

    boolean existsByFulfillmentOrderId(UUID fulfillmentOrderId);
}
