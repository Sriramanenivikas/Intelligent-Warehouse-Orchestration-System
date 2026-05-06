package com.iwos.controltower.api.http;

import com.iwos.controltower.application.ControlTowerQueryService;
import com.iwos.controltower.application.ControlTowerRefreshService;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/control-tower")
public class ControlTowerController {

    private final ControlTowerQueryService queryService;
    private final ControlTowerRefreshService refreshService;

    public ControlTowerController(ControlTowerQueryService queryService, ControlTowerRefreshService refreshService) {
        this.queryService = queryService;
        this.refreshService = refreshService;
    }

    @GetMapping("/latest")
    public ControlTowerSnapshotResponse latest() {
        return queryService.latestSnapshot();
    }

    @PostMapping("/refresh")
    public ControlTowerSnapshotResponse refreshNow() {
        return refreshService.refreshNow("MANUAL");
    }
}
