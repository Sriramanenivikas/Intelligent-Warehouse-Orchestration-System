package com.iwos.scanevent.api.http;

import com.iwos.scanevent.application.ScanEventQueryService;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/scan-events")
public class ScanTimelineController {

    private final ScanEventQueryService queryService;

    public ScanTimelineController(ScanEventQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/shipments/{shipmentId}")
    public ScanTimelineResponse getByShipmentId(@PathVariable("shipmentId") UUID shipmentId) {
        return queryService.getByShipmentId(shipmentId);
    }

    @GetMapping("/order-intents/{orderIntentId}")
    public ScanTimelineResponse getByOrderIntentId(@PathVariable("orderIntentId") UUID orderIntentId) {
        return queryService.getByOrderIntentId(orderIntentId);
    }

    @GetMapping("/awbs/{awbNumber}")
    public ScanTimelineResponse getByAwbNumber(@PathVariable("awbNumber") String awbNumber) {
        return queryService.getByAwbNumber(awbNumber);
    }
}
