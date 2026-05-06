package com.iwos.forecasting.infrastructure.persistence.repository;

import com.iwos.forecasting.infrastructure.persistence.entity.ForecastRunEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ForecastRunRepository extends JpaRepository<ForecastRunEntity, UUID> {

    Optional<ForecastRunEntity> findTopByOrderByStartedAtDesc();
}
