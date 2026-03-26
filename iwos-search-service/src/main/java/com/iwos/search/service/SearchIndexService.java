package com.iwos.search.service;

import com.iwos.search.model.ProductDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.DeleteRequest;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchIndexService {

    private final OpenSearchClient client;
    private static final String INDEX = "products";

    public void indexProduct(ProductDocument doc) {
        try {
            client.index(IndexRequest.of(i -> i.index(INDEX).id(doc.getId()).document(doc)));
            log.info("Indexed product: {}", doc.getSkuCode());
        } catch (IOException e) {
            log.error("Failed to index product: {}", doc.getId(), e);
        }
    }

    public void removeProduct(String productId) {
        try {
            client.delete(DeleteRequest.of(d -> d.index(INDEX).id(productId)));
            log.info("Removed product from index: {}", productId);
        } catch (IOException e) {
            log.error("Failed to remove product: {}", productId, e);
        }
    }
}
