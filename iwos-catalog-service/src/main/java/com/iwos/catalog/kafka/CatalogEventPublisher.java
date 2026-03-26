package com.iwos.catalog.kafka;

import com.iwos.catalog.entity.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class CatalogEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC = "catalog.events";

    public void publishProductCreated(Product product) {
        Map<String, Object> event = Map.of(
                "eventType", "PRODUCT_CREATED",
                "productId", product.getId(),
                "skuCode", product.getSkuCode(),
                "name", product.getName(),
                "sellingPrice", product.getSellingPrice(),
                "categoryId", product.getCategory().getId()
        );
        kafkaTemplate.send(TOPIC, product.getId(), event);
        log.info("Published PRODUCT_CREATED event for SKU: {}", product.getSkuCode());
    }

    public void publishProductUpdated(Product product) {
        Map<String, Object> event = Map.of(
                "eventType", "PRODUCT_UPDATED",
                "productId", product.getId(),
                "skuCode", product.getSkuCode(),
                "name", product.getName(),
                "sellingPrice", product.getSellingPrice(),
                "active", product.isActive()
        );
        kafkaTemplate.send(TOPIC, product.getId(), event);
        log.info("Published PRODUCT_UPDATED event for SKU: {}", product.getSkuCode());
    }

    public void publishProductDeleted(String productId) {
        Map<String, String> event = Map.of("eventType", "PRODUCT_DELETED", "productId", productId);
        kafkaTemplate.send(TOPIC, productId, event);
        log.info("Published PRODUCT_DELETED event for ID: {}", productId);
    }
}
