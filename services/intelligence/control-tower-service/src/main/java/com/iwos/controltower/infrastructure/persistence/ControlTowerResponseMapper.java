package com.iwos.controltower.infrastructure.persistence;

import com.iwos.controltower.api.http.ControlTowerSnapshotResponse;
import com.iwos.controltower.infrastructure.persistence.entity.ControlTowerSnapshotEntity;
import org.springframework.stereotype.Component;

@Component
public class ControlTowerResponseMapper {

    public ControlTowerSnapshotResponse read(ControlTowerSnapshotEntity entity) {
        throw new UnsupportedOperationException("Use ObjectMapper in service layer");
    }
}
