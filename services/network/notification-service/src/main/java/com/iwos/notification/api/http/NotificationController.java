package com.iwos.notification.api.http;

import com.iwos.notification.application.NotificationQueryService;
import com.iwos.notification.domain.notification.NotificationAudience;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationQueryService queryService;

    public NotificationController(NotificationQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/{notificationId}")
    public NotificationResponse getById(@PathVariable("notificationId") UUID notificationId) {
        return queryService.getById(notificationId);
    }

    @GetMapping("/order-intents/{orderIntentId}")
    public List<NotificationResponse> listByOrderIntentId(@PathVariable("orderIntentId") UUID orderIntentId) {
        return queryService.listByOrderIntentId(orderIntentId);
    }

    @GetMapping("/shipments/{shipmentId}")
    public List<NotificationResponse> listByShipmentId(@PathVariable("shipmentId") UUID shipmentId) {
        return queryService.listByShipmentId(shipmentId);
    }

    @GetMapping
    public List<NotificationResponse> listByAudience(
            @RequestParam(name = "audience") NotificationAudience audience
    ) {
        return queryService.listByAudience(audience);
    }
}
