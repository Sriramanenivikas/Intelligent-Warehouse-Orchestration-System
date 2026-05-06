package com.iwos.forecasting.infrastructure.persistence.repository;

import com.iwos.forecasting.infrastructure.persistence.entity.ForecastModelRunEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ForecastModelRunRepository extends JpaRepository<ForecastModelRunEntity, UUID> {

    Optional<ForecastModelRunEntity> findTopByOrderByTrainingCompletedAtDescCreatedAtDesc();
}
