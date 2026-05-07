package com.iwos.forecasting.api.http;

import com.iwos.forecasting.application.ForecastQueryService;
import com.iwos.forecasting.application.ForecastRefreshService;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class ForecastController {

    private final ForecastQueryService queryService;
    private final ForecastRefreshService refreshService;

    public ForecastController(ForecastQueryService queryService, ForecastRefreshService refreshService) {
        this.queryService = queryService;
        this.refreshService = refreshService;
    }

    @GetMapping("/forecasts")
    public List<InventoryForecastResponse> latestForecasts(
            @RequestParam(name = "nodeId", required = false) String nodeId,
            @RequestParam(name = "sku", required = false) String sku,
            @RequestParam(name = "risk", required = false) String risk,
            @RequestParam(name = "limit", defaultValue = "50") int limit
    ) {
        return queryService.listLatestForecasts(nodeId, sku, risk, limit);
    }

    @GetMapping("/forecasts/{forecastId}")
    public InventoryForecastResponse getById(@PathVariable("forecastId") UUID forecastId) {
        return queryService.getById(forecastId);
    }

    @GetMapping("/forecast-runs/latest")
    public ForecastRunResponse latestRun() {
        return queryService.getLatestRun();
    }

    @GetMapping("/model-runs/latest")
    public ForecastModelRunResponse latestModelRun() {
        return queryService.getLatestModelRun();
    }

    @GetMapping("/forecast-runs/latest/summary")
    public ForecastSummaryResponse latestSummary() {
        return queryService.getLatestSummary();
    }

    @PostMapping("/forecast-runs/refresh")
    public ForecastRunResponse refreshNow() {
        return refreshService.refreshNow("MANUAL");
    }
}
