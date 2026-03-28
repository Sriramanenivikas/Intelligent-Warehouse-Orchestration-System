package com.iwos.orderintake.infrastructure.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwos.orderintake.api.http.OrderIntentResponse;
import com.iwos.orderintake.infrastructure.config.OrderIntakeServiceProperties;
import java.util.Optional;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class IdempotencyCache {

    private static final String CACHE_KEY_PREFIX = "order-intake:idempotency:";

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final OrderIntakeServiceProperties properties;

    public IdempotencyCache(
            StringRedisTemplate stringRedisTemplate,
            ObjectMapper objectMapper,
            OrderIntakeServiceProperties properties
    ) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    public Optional<IdempotencyCacheEntry> find(String idempotencyKey) {
        String cachedValue = stringRedisTemplate.opsForValue().get(redisKey(idempotencyKey));
        if (cachedValue == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(cachedValue, IdempotencyCacheEntry.class));
        } catch (JsonProcessingException exception) {
            stringRedisTemplate.delete(redisKey(idempotencyKey));
            return Optional.empty();
        }
    }

    public void put(String idempotencyKey, String requestHash, OrderIntentResponse response) {
        try {
            IdempotencyCacheEntry entry = new IdempotencyCacheEntry(requestHash, response);
            stringRedisTemplate.opsForValue().set(
                    redisKey(idempotencyKey),
                    objectMapper.writeValueAsString(entry),
                    properties.getIdempotencyCacheTtl()
            );
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to cache idempotent response", exception);
        }
    }

    private String redisKey(String idempotencyKey) {
        return CACHE_KEY_PREFIX + idempotencyKey;
    }
}
