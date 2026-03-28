package com.iwos.inventoryledger.infrastructure.persistence.repository;

import com.iwos.inventoryledger.infrastructure.persistence.entity.InventoryReservationEntity;
import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface InventoryReservationRepository extends JpaRepository<InventoryReservationEntity, UUID> {

    Optional<InventoryReservationEntity> findByReservationId(UUID reservationId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select reservation from InventoryReservationEntity reservation where reservation.reservationId = :reservationId")
    Optional<InventoryReservationEntity> findByReservationIdForUpdate(UUID reservationId);
}
