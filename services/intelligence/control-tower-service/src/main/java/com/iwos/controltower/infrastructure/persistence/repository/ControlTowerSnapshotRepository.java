package com.iwos.controltower.infrastructure.persistence.repository;

import com.iwos.controltower.infrastructure.persistence.entity.ControlTowerSnapshotEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ControlTowerSnapshotRepository extends JpaRepository<ControlTowerSnapshotEntity, UUID> {

    Optional<ControlTowerSnapshotEntity> findTopByOrderByGeneratedAtDesc();
}
