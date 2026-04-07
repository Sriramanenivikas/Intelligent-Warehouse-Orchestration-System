package com.iwos.shipmenthandoff.infrastructure.taskexecution;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwos.shipmenthandoff.infrastructure.config.ShipmentHandoffServiceProperties;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class WarehouseOrchestratorClient {

    private static final Logger log = LoggerFactory.getLogger(WarehouseOrchestratorClient.class);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;

    public WarehouseOrchestratorClient(
            ShipmentHandoffServiceProperties properties,
            ObjectMapper objectMapper
    ) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = objectMapper;
        this.baseUrl = properties.warehouse().baseUrl();
    }

    public Optional<FulfillmentOrderResponse> getFulfillmentOrder(UUID fulfillmentOrderId) {
        try {
            String url = baseUrl + "/api/v1/fulfillment-orders/" + fulfillmentOrderId;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(30))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return Optional.of(objectMapper.readValue(response.body(), FulfillmentOrderResponse.class));
            } else if (response.statusCode() == 404) {
                log.warn("Fulfillment order not found: {}", fulfillmentOrderId);
                return Optional.empty();
            } else {
                log.error("Error fetching fulfillment order {}: status={}", fulfillmentOrderId, response.statusCode());
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("Failed to fetch fulfillment order: {}", fulfillmentOrderId, e);
            return Optional.empty();
        }
    }

    public Optional<FulfillmentOrderResponse> getFulfillmentOrderByOrderIntentId(UUID orderIntentId) {
        try {
            String url = baseUrl + "/api/v1/fulfillment-orders/by-order-intent/" + orderIntentId;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(30))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return Optional.of(objectMapper.readValue(response.body(), FulfillmentOrderResponse.class));
            } else if (response.statusCode() == 404) {
                return Optional.empty();
            } else {
                log.error("Error fetching fulfillment order by orderIntentId {}: status={}", orderIntentId, response.statusCode());
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("Failed to fetch fulfillment order by orderIntentId: {}", orderIntentId, e);
            return Optional.empty();
        }
    }
}
