package com.iwos.forecasting.infrastructure.persistence;

import com.iwos.forecasting.api.http.ForecastRunResponse;
import com.iwos.forecasting.api.http.InventoryForecastResponse;
import com.iwos.forecasting.infrastructure.persistence.entity.ForecastRunEntity;
import com.iwos.forecasting.infrastructure.persistence.entity.InventoryForecastEntity;
import org.springframework.stereotype.Component;

@Component
public class ForecastResponseMapper {

    public InventoryForecastResponse toResponse(InventoryForecastEntity forecast) {
        return new InventoryForecastResponse(
                forecast.getForecastId(),
                forecast.getForecastRunId(),
                forecast.getNodeId(),
                forecast.getSku(),
                forecast.getCurrentOnHandQuantity(),
                forecast.getCurrentReservedQuantity(),
                forecast.getAvailableQuantity(),
                forecast.getDemandLast1h(),
                forecast.getDemandLast6h(),
                forecast.getDemandLast24h(),
                forecast.getPredictedHourlyDemand(),
                forecast.getPredicted15mDemand(),
                forecast.getPredicted24hDemand(),
                forecast.getDaysOfCover(),
                forecast.getStockoutRisk(),
                forecast.getRecommendedReplenishmentQuantity(),
                forecast.isRecommendedReorder(),
                forecast.getModelVersion(),
                forecast.getGeneratedAt()
        );
    }

    public ForecastRunResponse toRunResponse(ForecastRunEntity run) {
        return new ForecastRunResponse(
                run.getForecastRunId(),
                run.getModelVersion(),
                run.getTriggeredBy(),
                run.getRunStatus(),
                run.getForecastCount(),
                run.getStartedAt(),
                run.getCompletedAt()
        );
    }
}
