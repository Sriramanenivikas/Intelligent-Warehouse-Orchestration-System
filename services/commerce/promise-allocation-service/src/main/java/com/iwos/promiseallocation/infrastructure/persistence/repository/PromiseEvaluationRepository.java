package com.iwos.promiseallocation.infrastructure.persistence.repository;

import com.iwos.promiseallocation.infrastructure.persistence.entity.PromiseEvaluationEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromiseEvaluationRepository extends JpaRepository<PromiseEvaluationEntity, UUID> {
}
