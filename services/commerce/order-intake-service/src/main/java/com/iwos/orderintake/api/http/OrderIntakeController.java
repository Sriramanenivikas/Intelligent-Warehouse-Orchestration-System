package com.iwos.orderintake.api.http;

import com.iwos.orderintake.application.OrderIntakeCommandService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Accepts new order intents from upstream callers.
 */
@Validated
@RestController
@RequestMapping("/api/v1/order-intents")
public class OrderIntakeController {

    private final OrderIntakeCommandService orderIntakeCommandService;

    public OrderIntakeController(OrderIntakeCommandService orderIntakeCommandService) {
        this.orderIntakeCommandService = orderIntakeCommandService;
    }

    @PostMapping
    public ResponseEntity<OrderIntentResponse> createOrderIntent(
            @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey,
            @Valid @RequestBody CreateOrderIntentRequest request
    ) {
        var result = orderIntakeCommandService.accept(idempotencyKey, request);

        return ResponseEntity.accepted()
                .header("Idempotency-Replayed", Boolean.toString(result.replayed()))
                .body(result.response());
    }
}
