package com.iwos.controltower.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwos.controltower.api.http.ControlTowerSnapshotResponse;
import com.iwos.controltower.api.http.ControlTowerSnapshotResponse.BucketCountResponse;
import com.iwos.controltower.api.http.ControlTowerSnapshotResponse.ControlTowerExceptionResponse;
import com.iwos.controltower.api.http.ControlTowerSnapshotResponse.ControlTowerForecastAlertResponse;
import com.iwos.controltower.api.http.ControlTowerSnapshotResponse.ForecastKpiResponse;
import com.iwos.controltower.infrastructure.cache.ControlTowerSnapshotCache;
import com.iwos.controltower.infrastructure.config.ControlTowerServiceProperties;
import com.iwos.controltower.infrastructure.persistence.entity.ControlTowerSnapshotEntity;
import com.iwos.controltower.infrastructure.persistence.repository.ControlTowerReadModelJdbcRepository;
import com.iwos.controltower.infrastructure.persistence.repository.ControlTowerSnapshotRepository;
import java.time.Instant;
import java.util.Collections;
import java.util.UUID;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ControlTowerRefreshService {

    private final ControlTowerReadModelJdbcRepository readModelRepository;
    private final ControlTowerSnapshotRepository snapshotRepository;
    private final ControlTowerSnapshotCache cache;
    private final ControlTowerServiceProperties properties;
    private final ObjectMapper objectMapper;

    public ControlTowerRefreshService(
            ControlTowerReadModelJdbcRepository readModelRepository,
            ControlTowerSnapshotRepository snapshotRepository,
            ControlTowerSnapshotCache cache,
            ControlTowerServiceProperties properties,
            ObjectMapper objectMapper
    ) {
        this.readModelRepository = readModelRepository;
        this.snapshotRepository = snapshotRepository;
        this.cache = cache;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Scheduled(cron = "${control-tower-service.scheduler.refresh-cron}")
    public void scheduledRefresh() {
        refreshNow("SCHEDULED");
    }

    @Transactional
    public ControlTowerSnapshotResponse refreshNow(String triggeredBy) {
        ControlTowerSnapshotResponse response = new ControlTowerSnapshotResponse(
                UUID.randomUUID(),
                triggeredBy,
                properties.serviceName(),
                Instant.now(),
                safeForecastKpi(),
                safeList(readModelRepository::loadOrderIntentStatuses),
                safeList(readModelRepository::loadFulfillmentOrderStatuses),
                safeList(readModelRepository::loadShipmentStatuses),
                safeList(readModelRepository::loadNetworkShipmentStatuses),
                safeList(readModelRepository::loadScanEventTypes),
                safeList(readModelRepository::loadNotificationAudienceCounts),
                safeList(readModelRepository::loadNotificationStatusCounts),
                safeForecastAlerts(),
                safeRecentExceptions()
        );
        try {
            cache.put(response);
            ControlTowerSnapshotEntity entity = new ControlTowerSnapshotEntity();
            entity.setControlTowerSnapshotId(response.controlTowerSnapshotId());
            entity.setSnapshotType(triggeredBy);
            entity.setModelVersion(response.modelVersion());
            entity.setPayloadJson(objectMapper.writeValueAsString(response));
            entity.setGeneratedAt(response.generatedAt());
            snapshotRepository.save(entity);
        } catch (Exception exception) {
            // Keep the demo path live even if persistence has a problem.
        }
        return response;
    }

    private ForecastKpiResponse safeForecastKpi() {
        try {
            return readModelRepository.loadForecastKpi();
        } catch (Exception exception) {
            return new ForecastKpiResponse(0, 0, 0, 0, 0, 0);
        }
    }

    private java.util.List<BucketCountResponse> safeList(SupplierWithException<java.util.List<BucketCountResponse>> supplier) {
        try {
            return supplier.get();
        } catch (Exception exception) {
            return Collections.emptyList();
        }
    }

    private java.util.List<ControlTowerForecastAlertResponse> safeForecastAlerts() {
        try {
            return readModelRepository.loadTopForecastAlerts(properties.snapshot().topLimit());
        } catch (Exception exception) {
            return Collections.emptyList();
        }
    }

    private java.util.List<ControlTowerExceptionResponse> safeRecentExceptions() {
        try {
            return readModelRepository.loadRecentExceptions(properties.snapshot().topLimit());
        } catch (Exception exception) {
            return Collections.emptyList();
        }
    }

    @FunctionalInterface
    private interface SupplierWithException<T> {
        T get() throws Exception;
    }
}
