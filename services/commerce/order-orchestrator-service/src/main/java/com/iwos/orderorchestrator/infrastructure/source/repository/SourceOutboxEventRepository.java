package com.iwos.orderorchestrator.infrastructure.source.repository;

import com.iwos.orderorchestrator.infrastructure.source.entity.SourceOutboxEventEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SourceOutboxEventRepository extends JpaRepository<SourceOutboxEventEntity, UUID> {

    @Query(
            value = """
                    select source.*
                    from public.outbox_events source
                    where source.event_type = 'order-intake.accepted.v1'
                      and not exists (
                          select 1
                          from order_orchestration.order_workflows workflow
                          where workflow.source_outbox_event_id = source.outbox_event_id
                      )
                    order by source.created_at
                    limit :limit
                    """,
            nativeQuery = true
    )
    List<SourceOutboxEventEntity> findPendingAcceptedOrderIntentEvents(@Param("limit") int limit);

    Optional<SourceOutboxEventEntity> findFirstByAggregateIdAndEventTypeOrderByCreatedAtAsc(
            UUID aggregateId,
            String eventType
    );
}
