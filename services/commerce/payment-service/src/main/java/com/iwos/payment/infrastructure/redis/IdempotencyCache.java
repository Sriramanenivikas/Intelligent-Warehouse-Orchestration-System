package com.iwos.payment.infrastructure.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwos.payment.api.http.PaymentIntentResponse;
import com.iwos.payment.infrastructure.config.PaymentServiceProperties;
import java.util.Optional;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class IdempotencyCache {

    private static final String CACHE_KEY_PREFIX = "payment:idempotency:";

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final PaymentServiceProperties properties;

    public IdempotencyCache(
            StringRedisTemplate stringRedisTemplate,
            ObjectMapper objectMapper,
            PaymentServiceProperties properties
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

    public void put(String idempotencyKey, String requestHash, PaymentIntentResponse response) {
        try {
            IdempotencyCacheEntry entry = new IdempotencyCacheEntry(requestHash, response);
            stringRedisTemplate.opsForValue().set(
                    redisKey(idempotencyKey),
                    objectMapper.writeValueAsString(entry),
                    properties.getIdempotencyCacheTtl()
            );
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to cache idempotent payment response", exception);
        }
    }

    private String redisKey(String idempotencyKey) {
        return CACHE_KEY_PREFIX + idempotencyKey;
    }
}
