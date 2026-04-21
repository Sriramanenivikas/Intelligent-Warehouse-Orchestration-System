package com.iwos.shipmentnetwork.infrastructure.persistence.repository;

import com.iwos.shipmentnetwork.domain.network.NetworkShipmentStatus;
import com.iwos.shipmentnetwork.infrastructure.persistence.entity.NetworkShipmentEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NetworkShipmentRepository extends JpaRepository<NetworkShipmentEntity, UUID> {

    Optional<NetworkShipmentEntity> findByShipmentId(UUID shipmentId);

    Optional<NetworkShipmentEntity> findByOrderIntentId(UUID orderIntentId);

    Optional<NetworkShipmentEntity> findByAwbNumber(String awbNumber);

    boolean existsByShipmentId(UUID shipmentId);

    long countByStatus(NetworkShipmentStatus status);
}
