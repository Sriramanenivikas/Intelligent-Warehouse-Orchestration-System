package com.iwos.orderorchestrator.infrastructure.source.repository;

import com.iwos.orderorchestrator.infrastructure.source.entity.SourceOrderIntentEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SourceOrderIntentRepository extends JpaRepository<SourceOrderIntentEntity, UUID> {

    @Query("""
            select distinct orderIntent
            from SourceOrderIntentEntity orderIntent
            left join fetch orderIntent.items items
            where orderIntent.orderIntentId = :orderIntentId
            """)
    Optional<SourceOrderIntentEntity> findWithItemsByOrderIntentId(UUID orderIntentId);
}
