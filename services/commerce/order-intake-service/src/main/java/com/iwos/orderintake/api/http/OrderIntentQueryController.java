package com.iwos.orderintake.api.http;

import com.iwos.orderintake.application.OrderIntentQueryService;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes lookup access for accepted order intents.
 */
@RestController
@RequestMapping("/api/v1/order-intents")
public class OrderIntentQueryController {

    private final OrderIntentQueryService orderIntentQueryService;

    public OrderIntentQueryController(OrderIntentQueryService orderIntentQueryService) {
        this.orderIntentQueryService = orderIntentQueryService;
    }

    @GetMapping("/{orderIntentId}")
    public OrderIntentResponse getOrderIntent(@PathVariable("orderIntentId") UUID orderIntentId) {
        return orderIntentQueryService.get(orderIntentId);
    }
}
