package com.iwos.controltower.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwos.controltower.api.http.ControlTowerSnapshotResponse;
import com.iwos.controltower.domain.ControlTowerNotFoundException;
import com.iwos.controltower.infrastructure.cache.ControlTowerSnapshotCache;
import com.iwos.controltower.infrastructure.persistence.entity.ControlTowerSnapshotEntity;
import com.iwos.controltower.infrastructure.persistence.repository.ControlTowerSnapshotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ControlTowerQueryService {

    private final ControlTowerSnapshotRepository snapshotRepository;
    private final ControlTowerSnapshotCache cache;
    private final ObjectMapper objectMapper;

    public ControlTowerQueryService(
            ControlTowerSnapshotRepository snapshotRepository,
            ControlTowerSnapshotCache cache,
            ObjectMapper objectMapper
    ) {
        this.snapshotRepository = snapshotRepository;
        this.cache = cache;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public ControlTowerSnapshotResponse latestSnapshot() {
        return cache.get().orElseGet(() -> snapshotRepository.findTopByOrderByGeneratedAtDesc()
                .map(this::read)
                .orElseThrow(ControlTowerNotFoundException::new));
    }

    private ControlTowerSnapshotResponse read(ControlTowerSnapshotEntity entity) {
        try {
            return objectMapper.readValue(entity.getPayloadJson(), ControlTowerSnapshotResponse.class);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to read control tower snapshot payload", exception);
        }
    }
}
