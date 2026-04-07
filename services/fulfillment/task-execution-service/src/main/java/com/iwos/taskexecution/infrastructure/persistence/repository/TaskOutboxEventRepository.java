package com.iwos.taskexecution.infrastructure.persistence.repository;

import com.iwos.taskexecution.infrastructure.persistence.entity.TaskOutboxEventEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskOutboxEventRepository extends JpaRepository<TaskOutboxEventEntity, UUID> {

    @Query("SELECT e FROM TaskOutboxEventEntity e WHERE e.status = 'PENDING' ORDER BY e.createdAt ASC")
    List<TaskOutboxEventEntity> findPendingEvents(Pageable pageable);

    long countByStatus(String status);
}
