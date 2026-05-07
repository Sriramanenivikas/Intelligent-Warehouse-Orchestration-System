package com.iwos.forecasting.application;

import com.iwos.forecasting.api.http.ForecastRunResponse;
import com.iwos.forecasting.domain.ForecastDemandSnapshot;
import com.iwos.forecasting.domain.ForecastPlanner;
import com.iwos.forecasting.infrastructure.config.ForecastingPlanningServiceProperties;
import com.iwos.forecasting.infrastructure.persistence.ForecastResponseMapper;
import com.iwos.forecasting.infrastructure.persistence.entity.ForecastRunEntity;
import com.iwos.forecasting.infrastructure.persistence.entity.InventoryForecastEntity;
import com.iwos.forecasting.infrastructure.persistence.repository.ForecastRunRepository;
import com.iwos.forecasting.infrastructure.persistence.repository.InventoryDemandSignalJdbcRepository;
import com.iwos.forecasting.infrastructure.persistence.repository.InventoryForecastRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ForecastRefreshService {

    private final InventoryDemandSignalJdbcRepository demandSignalRepository;
    private final InventoryForecastRepository forecastRepository;
    private final ForecastRunRepository runRepository;
    private final ForecastPlanner planner;
    private final ForecastingPlanningServiceProperties properties;
    private final ForecastResponseMapper mapper;

    public ForecastRefreshService(
            InventoryDemandSignalJdbcRepository demandSignalRepository,
            InventoryForecastRepository forecastRepository,
            ForecastRunRepository runRepository,
            ForecastPlanner planner,
            ForecastingPlanningServiceProperties properties,
            ForecastResponseMapper mapper
    ) {
        this.demandSignalRepository = demandSignalRepository;
        this.forecastRepository = forecastRepository;
        this.runRepository = runRepository;
        this.planner = planner;
        this.properties = properties;
        this.mapper = mapper;
    }

    @Scheduled(cron = "${forecasting-planning-service.scheduler.refresh-cron}")
    public void scheduledRefresh() {
        if (!heuristicModeEnabled()) {
            return;
        }
        refreshNow("SCHEDULED");
    }

    @Transactional
    public ForecastRunResponse refreshNow(String triggeredBy) {
        if (!heuristicModeEnabled()) {
            throw new IllegalStateException("Manual heuristic refresh is disabled when external ML pipeline mode is active");
        }
        Instant now = Instant.now();
        ForecastRunEntity run = new ForecastRunEntity();
        run.setForecastRunId(UUID.randomUUID());
        run.setModelVersion(properties.model().version());
        run.setTriggeredBy(triggeredBy);
        run.setRunStatus("RUNNING");
        run.setForecastCount(0);
        run.setStartedAt(now);
        runRepository.save(run);

        List<ForecastDemandSnapshot> snapshots = demandSignalRepository.loadDemandSnapshots();
        List<InventoryForecastEntity> forecasts = snapshots.stream()
                .map(snapshot -> planner.plan(run.getForecastRunId(), snapshot, now))
                .toList();
        forecastRepository.saveAll(forecasts);

        run.setForecastCount(forecasts.size());
        run.setRunStatus("COMPLETED");
        run.setCompletedAt(Instant.now());
        runRepository.save(run);
        return mapper.toRunResponse(run);
    }

    private boolean heuristicModeEnabled() {
        return properties.pipeline() == null
                || properties.pipeline().mode() == null
                || "HEURISTIC".equalsIgnoreCase(properties.pipeline().mode());
    }
}
