package com.iwos.warehouseorchestrator.infrastructure.orderworkflow;

import com.iwos.warehouseorchestrator.infrastructure.config.WarehouseOrchestratorServiceProperties;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class OrderWorkflowClient {

    private static final Logger log = LoggerFactory.getLogger(OrderWorkflowClient.class);

    private final RestClient restClient;

    public OrderWorkflowClient(WarehouseOrchestratorServiceProperties properties, RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder
                .baseUrl(properties.getOrderOrchestratorBaseUrl())
                .build();
    }

    public OrderWorkflowClientResponse fetchWorkflow(UUID orderIntentId) {
        log.info("Fetching order workflow for orderIntentId={}", orderIntentId);
        try {
            return restClient.get()
                    .uri("/api/v1/order-workflows/{orderIntentId}", orderIntentId)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        String body;
                        try {
                            body = new String(response.getBody().readAllBytes());
                        } catch (Exception e) {
                            body = "(unreadable body)";
                        }
                        log.error("Order orchestrator returned HTTP {} for orderIntentId={}: {}",
                                response.getStatusCode().value(), orderIntentId, body);
                        throw new OrderWorkflowClientException(orderIntentId, response.getStatusCode().value(), body);
                    })
                    .body(OrderWorkflowClientResponse.class);
        } catch (OrderWorkflowClientException e) {
            throw e;
        } catch (RestClientException e) {
            throw new OrderWorkflowClientException(orderIntentId, e);
        }
    }
}
