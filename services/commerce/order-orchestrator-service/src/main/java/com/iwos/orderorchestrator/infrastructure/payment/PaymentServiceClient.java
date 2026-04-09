package com.iwos.orderorchestrator.infrastructure.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwos.orderorchestrator.infrastructure.config.OrderOrchestratorServiceProperties;
import com.iwos.orderorchestrator.infrastructure.observability.OrderWorkflowMetrics;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class PaymentServiceClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final OrderWorkflowMetrics orderWorkflowMetrics;

    public PaymentServiceClient(
            RestClient.Builder restClientBuilder,
            OrderOrchestratorServiceProperties properties,
            ObjectMapper objectMapper,
            OrderWorkflowMetrics orderWorkflowMetrics
    ) {
        this.restClient = restClientBuilder
                .baseUrl(properties.getPaymentServiceBaseUrl())
                .build();
        this.objectMapper = objectMapper;
        this.orderWorkflowMetrics = orderWorkflowMetrics;
    }

    public PaymentIntentClientResponse createPaymentIntent(
            String idempotencyKey,
            CreatePaymentIntentRequest request
    ) {
        var clientTimer = orderWorkflowMetrics.startPaymentClientTimer();
        try {
            PaymentIntentClientResponse response = restClient.post()
                    .uri("/api/v1/payments")
                    .header("Idempotency-Key", idempotencyKey)
                    .body(request)
                    .retrieve()
                    .body(PaymentIntentClientResponse.class);
            orderWorkflowMetrics.recordPaymentClientCall("create_payment_intent", "success", clientTimer);
            return response;
        } catch (RestClientResponseException exception) {
            orderWorkflowMetrics.recordPaymentClientCall("create_payment_intent", "error", clientTimer);
            throw mapResponseException(exception);
        } catch (RestClientException exception) {
            orderWorkflowMetrics.recordPaymentClientCall("create_payment_intent", "error", clientTimer);
            throw new PaymentServiceClientException(
                    HttpStatus.BAD_GATEWAY,
                    "Payment service call failed while creating payment intent",
                    exception
            );
        }
    }

    private PaymentServiceClientException mapResponseException(RestClientResponseException exception) {
        String message = "Payment service request failed with status %s".formatted(exception.getStatusCode().value());
        try {
            PaymentServiceErrorResponse error = objectMapper.readValue(
                    exception.getResponseBodyAsByteArray(),
                    PaymentServiceErrorResponse.class
            );
            if (error.code() != null && error.message() != null) {
                message = error.code() + ": " + error.message();
            }
        } catch (IOException ignored) {
            if (exception.getResponseBodyAsString() != null && !exception.getResponseBodyAsString().isBlank()) {
                message = exception.getResponseBodyAsString();
            }
        }

        return new PaymentServiceClientException(HttpStatus.valueOf(exception.getStatusCode().value()), message, exception);
    }
}
