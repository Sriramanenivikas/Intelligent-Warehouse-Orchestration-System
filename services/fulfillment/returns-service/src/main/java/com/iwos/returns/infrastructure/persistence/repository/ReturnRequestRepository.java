package com.iwos.returns.infrastructure.persistence.repository;

import com.iwos.returns.infrastructure.persistence.entity.ReturnRequestEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ReturnRequestRepository
        extends JpaRepository<ReturnRequestEntity, UUID>, JpaSpecificationExecutor<ReturnRequestEntity> {

    List<ReturnRequestEntity> findAllByOrderByRequestedAtDesc();
}
