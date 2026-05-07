package com.iwos.forecasting.infrastructure.persistence.repository;

import com.iwos.forecasting.infrastructure.persistence.entity.InventoryForecastEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventoryForecastRepository extends JpaRepository<InventoryForecastEntity, UUID> {

    List<InventoryForecastEntity> findByForecastRunId(UUID forecastRunId);

    @Query(value = """
            SELECT forecast.*
            FROM forecasting_planning.inventory_forecasts forecast
            JOIN (
                SELECT node_id, sku, MAX(generated_at) AS latest_generated_at
                FROM forecasting_planning.inventory_forecasts
                GROUP BY node_id, sku
            ) latest
              ON latest.node_id = forecast.node_id
             AND latest.sku = forecast.sku
             AND latest.latest_generated_at = forecast.generated_at
            WHERE (:nodeId IS NULL OR forecast.node_id = :nodeId)
              AND (:sku IS NULL OR forecast.sku = :sku)
              AND (:risk IS NULL OR forecast.stockout_risk = :risk)
            ORDER BY
              CASE forecast.stockout_risk
                WHEN 'CRITICAL' THEN 1
                WHEN 'HIGH' THEN 2
                WHEN 'MEDIUM' THEN 3
                ELSE 4
              END,
              forecast.generated_at DESC,
              forecast.node_id,
              forecast.sku
            LIMIT :limit
            """, nativeQuery = true)
    List<InventoryForecastEntity> findLatestForecasts(
            @Param("nodeId") String nodeId,
            @Param("sku") String sku,
            @Param("risk") String risk,
            @Param("limit") int limit
    );
}
