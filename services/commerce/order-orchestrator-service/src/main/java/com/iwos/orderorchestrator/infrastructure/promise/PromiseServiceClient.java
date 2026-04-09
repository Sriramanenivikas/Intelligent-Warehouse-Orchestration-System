package com.iwos.orderorchestrator.infrastructure.promise;

import com.iwos.orderorchestrator.infrastructure.config.OrderOrchestratorServiceProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class PromiseServiceClient {

    private final RestClient restClient;

    public PromiseServiceClient(OrderOrchestratorServiceProperties properties) {
        this.restClient = RestClient.builder()
                .baseUrl(properties.getPromiseServiceBaseUrl())
                .build();
    }

    public PromiseAllocationClientResponse resolvePromise(ResolvePromiseRequest request) {
        try {
            return restClient.post()
                    .uri("/api/v1/promises/resolve")
                    .body(request)
                    .retrieve()
                    .body(PromiseAllocationClientResponse.class);
        } catch (RestClientResponseException exception) {
            throw new PromiseAllocationClientException(
                    HttpStatus.valueOf(exception.getStatusCode().value()),
                    exception.getResponseBodyAsString() == null || exception.getResponseBodyAsString().isBlank()
                            ? "Promise allocation request failed with status %s".formatted(exception.getStatusCode().value())
                            : exception.getResponseBodyAsString(),
                    exception
            );
        } catch (RestClientException exception) {
            throw new PromiseAllocationClientException(
                    HttpStatus.BAD_GATEWAY,
                    "Promise allocation request failed",
                    exception
            );
        }
    }
}
