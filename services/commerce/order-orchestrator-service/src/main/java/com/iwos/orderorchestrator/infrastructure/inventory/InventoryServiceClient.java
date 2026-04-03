package com.iwos.orderorchestrator.infrastructure.inventory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwos.orderorchestrator.infrastructure.config.OrderOrchestratorServiceProperties;
import com.iwos.orderorchestrator.infrastructure.observability.OrderWorkflowMetrics;
import java.io.IOException;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class InventoryServiceClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final OrderWorkflowMetrics orderWorkflowMetrics;

    public InventoryServiceClient(
            RestClient.Builder restClientBuilder,
            OrderOrchestratorServiceProperties properties,
            ObjectMapper objectMapper,
            OrderWorkflowMetrics orderWorkflowMetrics
    ) {
        this.restClient = restClientBuilder
                .baseUrl(properties.getInventoryServiceBaseUrl())
                .build();
        this.objectMapper = objectMapper;
        this.orderWorkflowMetrics = orderWorkflowMetrics;
    }

    public InventoryReservationClientResponse createReservation(
            String idempotencyKey,
            InventoryCreateReservationRequest request
    ) {
        var clientTimer = orderWorkflowMetrics.startInventoryClientTimer();
        try {
            InventoryReservationClientResponse response = restClient.post()
                    .uri("/api/v1/reservations")
                    .header("Idempotency-Key", idempotencyKey)
                    .body(request)
                    .retrieve()
                    .body(InventoryReservationClientResponse.class);
            orderWorkflowMetrics.recordInventoryClientCall("create_reservation", "success", clientTimer);
            return response;
        } catch (RestClientResponseException exception) {
            orderWorkflowMetrics.recordInventoryClientCall("create_reservation", "error", clientTimer);
            throw mapResponseException(exception);
        } catch (RestClientException exception) {
            orderWorkflowMetrics.recordInventoryClientCall("create_reservation", "error", clientTimer);
            throw new InventoryServiceClientException(
                    HttpStatus.BAD_GATEWAY,
                    "Inventory service call failed while creating reservation",
                    exception
            );
        }
    }

    public InventoryReservationClientResponse releaseReservation(
            UUID reservationId,
            String idempotencyKey,
            InventoryReservationActionRequest request
    ) {
        var clientTimer = orderWorkflowMetrics.startInventoryClientTimer();
        try {
            InventoryReservationClientResponse response = restClient.post()
                    .uri("/api/v1/reservations/{reservationId}/release", reservationId)
                    .header("Idempotency-Key", idempotencyKey)
                    .body(request)
                    .retrieve()
                    .body(InventoryReservationClientResponse.class);
            orderWorkflowMetrics.recordInventoryClientCall("release_reservation", "success", clientTimer);
            return response;
        } catch (RestClientResponseException exception) {
            orderWorkflowMetrics.recordInventoryClientCall("release_reservation", "error", clientTimer);
            throw mapResponseException(exception);
        } catch (RestClientException exception) {
            orderWorkflowMetrics.recordInventoryClientCall("release_reservation", "error", clientTimer);
            throw new InventoryServiceClientException(
                    HttpStatus.BAD_GATEWAY,
                    "Inventory service call failed while releasing reservation",
                    exception
            );
        }
    }

    private InventoryServiceClientException mapResponseException(RestClientResponseException exception) {
        String message = "Inventory service request failed with status %s".formatted(exception.getStatusCode().value());
        try {
            InventoryServiceErrorResponse error = objectMapper.readValue(
                    exception.getResponseBodyAsByteArray(),
                    InventoryServiceErrorResponse.class
            );
            if (error.code() != null && error.message() != null) {
                message = error.code() + ": " + error.message();
            }
        } catch (IOException ignored) {
            message = exception.getResponseBodyAsString() == null || exception.getResponseBodyAsString().isBlank()
                    ? message
                    : exception.getResponseBodyAsString();
        }

        return new InventoryServiceClientException(HttpStatus.valueOf(exception.getStatusCode().value()), message, exception);
    }
}
