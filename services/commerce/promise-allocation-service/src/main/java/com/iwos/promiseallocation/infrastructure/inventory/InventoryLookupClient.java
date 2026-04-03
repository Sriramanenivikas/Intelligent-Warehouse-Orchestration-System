package com.iwos.promiseallocation.infrastructure.inventory;

import com.iwos.promiseallocation.infrastructure.config.PromiseAllocationServiceProperties;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class InventoryLookupClient {

    private final RestClient restClient;

    public InventoryLookupClient(PromiseAllocationServiceProperties properties) {
        this.restClient = RestClient.builder()
                .baseUrl(properties.getInventoryServiceBaseUrl())
                .build();
    }

    public Optional<InventoryStockClientResponse> getStock(String nodeId, String sku) {
        try {
            InventoryStockClientResponse response = restClient.get()
                    .uri("/api/v1/stock-items/{nodeId}/{sku}", nodeId, sku)
                    .retrieve()
                    .body(InventoryStockClientResponse.class);
            return Optional.ofNullable(response);
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode() == HttpStatus.NOT_FOUND) {
                return Optional.empty();
            }
            throw new InventoryLookupException(
                    "Inventory lookup failed for nodeId=%s sku=%s with status=%s"
                            .formatted(nodeId, sku, exception.getStatusCode().value()),
                    exception
            );
        } catch (RestClientException exception) {
            throw new InventoryLookupException(
                    "Inventory lookup failed for nodeId=%s sku=%s".formatted(nodeId, sku),
                    exception
            );
        }
    }
}
