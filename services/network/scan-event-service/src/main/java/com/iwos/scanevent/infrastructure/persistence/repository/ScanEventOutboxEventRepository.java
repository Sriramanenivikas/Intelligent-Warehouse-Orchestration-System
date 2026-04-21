package com.iwos.scanevent.infrastructure.persistence.repository;

import com.iwos.scanevent.infrastructure.persistence.entity.ScanEventOutboxEventEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ScanEventOutboxEventRepository extends JpaRepository<ScanEventOutboxEventEntity, UUID> {

    @Query("SELECT e FROM ScanEventOutboxEventEntity e WHERE e.status = 'PENDING' ORDER BY e.createdAt ASC")
    List<ScanEventOutboxEventEntity> findPendingEvents(Pageable pageable);

    long countByStatus(String status);
}
