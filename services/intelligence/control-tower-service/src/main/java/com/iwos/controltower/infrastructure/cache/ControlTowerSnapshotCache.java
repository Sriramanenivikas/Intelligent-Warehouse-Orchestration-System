package com.iwos.controltower.infrastructure.cache;

import com.iwos.controltower.api.http.ControlTowerSnapshotResponse;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Component;

@Component
public class ControlTowerSnapshotCache {

    private final AtomicReference<ControlTowerSnapshotResponse> latest = new AtomicReference<>();

    public void put(ControlTowerSnapshotResponse response) {
        latest.set(response);
    }

    public Optional<ControlTowerSnapshotResponse> get() {
        return Optional.ofNullable(latest.get());
    }
}
