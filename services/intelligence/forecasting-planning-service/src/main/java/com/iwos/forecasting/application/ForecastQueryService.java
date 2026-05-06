package com.iwos.forecasting.application;

import com.iwos.forecasting.api.http.ForecastRunResponse;
import com.iwos.forecasting.api.http.InventoryForecastResponse;
import com.iwos.forecasting.domain.ForecastNotFoundException;
import com.iwos.forecasting.domain.ForecastRunNotFoundException;
import com.iwos.forecasting.infrastructure.persistence.ForecastResponseMapper;
import com.iwos.forecasting.infrastructure.persistence.entity.ForecastRunEntity;
import com.iwos.forecasting.infrastructure.persistence.entity.InventoryForecastEntity;
import com.iwos.forecasting.infrastructure.persistence.repository.ForecastRunRepository;
import com.iwos.forecasting.infrastructure.persistence.repository.InventoryForecastRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ForecastQueryService {

    private final InventoryForecastRepository forecastRepository;
    private final ForecastRunRepository runRepository;
    private final ForecastResponseMapper mapper;

    public ForecastQueryService(
            InventoryForecastRepository forecastRepository,
            ForecastRunRepository runRepository,
            ForecastResponseMapper mapper
    ) {
        this.forecastRepository = forecastRepository;
        this.runRepository = runRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<InventoryForecastResponse> listLatestForecasts(String nodeId, String sku, String risk, int limit) {
        int boundedLimit = Math.max(1, Math.min(limit, 200));
        return forecastRepository.findLatestForecasts(nodeId, sku, risk, boundedLimit)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public InventoryForecastResponse getById(UUID forecastId) {
        InventoryForecastEntity forecast = forecastRepository.findById(forecastId)
                .orElseThrow(() -> new ForecastNotFoundException(forecastId));
        return mapper.toResponse(forecast);
    }

    @Transactional(readOnly = true)
    public ForecastRunResponse getLatestRun() {
        ForecastRunEntity run = runRepository.findTopByOrderByStartedAtDesc()
                .orElseThrow(ForecastRunNotFoundException::new);
        return mapper.toRunResponse(run);
    }
}
