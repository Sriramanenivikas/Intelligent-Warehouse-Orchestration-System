package com.iwos.search.kafka;

import com.iwos.search.model.ProductDocument;
import com.iwos.search.service.SearchIndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class CatalogEventConsumer {

    private final SearchIndexService indexService;

    @KafkaListener(topics = "catalog.events", groupId = "search-service")
    public void handleCatalogEvent(Map<String, Object> event) {
        String eventType = (String) event.get("eventType");
        log.info("Received catalog event: {}", eventType);

        switch (eventType) {
            case "PRODUCT_CREATED", "PRODUCT_UPDATED" -> {
                ProductDocument doc = ProductDocument.builder()
                        .id((String) event.get("productId"))
                        .skuCode((String) event.get("skuCode"))
                        .name((String) event.get("name"))
                        .sellingPrice(new BigDecimal(event.get("sellingPrice").toString()))
                        .active(event.get("active") != null ? (Boolean) event.get("active") : true)
                        .build();
                indexService.indexProduct(doc);
            }
            case "PRODUCT_DELETED" -> indexService.removeProduct((String) event.get("productId"));
            default -> log.warn("Unknown catalog event type: {}", eventType);
        }
    }
}
