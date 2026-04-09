package com.iwos.taskexecution.infrastructure.persistence.repository;

import com.iwos.taskexecution.domain.task.TaskStatus;
import com.iwos.taskexecution.domain.task.TaskType;
import com.iwos.taskexecution.infrastructure.persistence.entity.TaskAssignmentEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskAssignmentRepository extends JpaRepository<TaskAssignmentEntity, UUID> {

    Optional<TaskAssignmentEntity> findByFulfillmentTaskId(UUID fulfillmentTaskId);

    @Query("SELECT t FROM TaskAssignmentEntity t WHERE t.status = :status AND t.nodeId = :nodeId ORDER BY t.sourceCreatedAt ASC")
    List<TaskAssignmentEntity> findByStatusAndNodeIdOrderBySourceCreatedAtAsc(
            @Param("status") TaskStatus status,
            @Param("nodeId") String nodeId,
            Pageable pageable
    );

    @Query("SELECT t FROM TaskAssignmentEntity t WHERE t.status = :status ORDER BY t.sourceCreatedAt ASC")
    List<TaskAssignmentEntity> findByStatusOrderBySourceCreatedAtAsc(
            @Param("status") TaskStatus status,
            Pageable pageable
    );

    List<TaskAssignmentEntity> findByFulfillmentOrderIdAndTaskType(UUID fulfillmentOrderId, TaskType taskType);

    List<TaskAssignmentEntity> findByFulfillmentOrderId(UUID fulfillmentOrderId);

    @Query("SELECT COUNT(t) FROM TaskAssignmentEntity t WHERE t.fulfillmentOrderId = :fulfillmentOrderId AND t.taskType = :taskType AND t.status = :status")
    long countByFulfillmentOrderIdAndTaskTypeAndStatus(
            @Param("fulfillmentOrderId") UUID fulfillmentOrderId,
            @Param("taskType") TaskType taskType,
            @Param("status") TaskStatus status
    );

    boolean existsByFulfillmentTaskId(UUID fulfillmentTaskId);
}
