package com.iwos.payment.api.http;

import com.iwos.payment.application.PaymentCommandService;
import com.iwos.payment.application.PaymentQueryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/payments")
public class PaymentIntentController {

    private final PaymentCommandService paymentCommandService;
    private final PaymentQueryService paymentQueryService;

    public PaymentIntentController(
            PaymentCommandService paymentCommandService,
            PaymentQueryService paymentQueryService
    ) {
        this.paymentCommandService = paymentCommandService;
        this.paymentQueryService = paymentQueryService;
    }

    @PostMapping
    public ResponseEntity<PaymentIntentResponse> createPaymentIntent(
            @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey,
            @Valid @RequestBody CreatePaymentIntentRequest request
    ) {
        var result = paymentCommandService.authorize(idempotencyKey, request);
        return ResponseEntity.accepted()
                .header("Idempotency-Replayed", Boolean.toString(result.replayed()))
                .body(result.response());
    }

    @GetMapping("/{paymentIntentId}")
    public PaymentIntentResponse getPaymentIntent(@PathVariable UUID paymentIntentId) {
        return paymentQueryService.findById(paymentIntentId);
    }

    @PostMapping("/{paymentIntentId}/success")
    public PaymentIntentResponse markPaymentSucceeded(
            @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey,
            @PathVariable UUID paymentIntentId,
            @RequestBody(required = false) PaymentActionRequest request
    ) {
        return paymentCommandService.succeed(idempotencyKey, paymentIntentId, request == null ? new PaymentActionRequest(null, null, null) : request).response();
    }

    @PostMapping("/{paymentIntentId}/fail")
    public PaymentIntentResponse markPaymentFailed(
            @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey,
            @PathVariable UUID paymentIntentId,
            @RequestBody(required = false) PaymentActionRequest request
    ) {
        return paymentCommandService.fail(idempotencyKey, paymentIntentId, request == null ? new PaymentActionRequest(null, null, null) : request).response();
    }
}
