package com.iwos.inventoryledger.infrastructure.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwos.inventoryledger.infrastructure.config.InventoryLedgerServiceProperties;
import java.util.Optional;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class CommandIdempotencyCache {

    private static final String CACHE_KEY_PREFIX = "inventory-ledger:idempotency:";

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final InventoryLedgerServiceProperties properties;

    public CommandIdempotencyCache(
            StringRedisTemplate stringRedisTemplate,
            ObjectMapper objectMapper,
            InventoryLedgerServiceProperties properties
    ) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    public Optional<CommandIdempotencyCacheEntry> find(String idempotencyKey) {
        String cachedValue = stringRedisTemplate.opsForValue().get(redisKey(idempotencyKey));
        if (cachedValue == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(cachedValue, CommandIdempotencyCacheEntry.class));
        } catch (JsonProcessingException exception) {
            stringRedisTemplate.delete(redisKey(idempotencyKey));
            return Optional.empty();
        }
    }

    public void put(String idempotencyKey, String operationType, String requestHash, String responseBody) {
        try {
            CommandIdempotencyCacheEntry entry = new CommandIdempotencyCacheEntry(operationType, requestHash, responseBody);
            stringRedisTemplate.opsForValue().set(
                    redisKey(idempotencyKey),
                    objectMapper.writeValueAsString(entry),
                    properties.getIdempotencyCacheTtl()
            );
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to cache idempotent command response", exception);
        }
    }

    private String redisKey(String idempotencyKey) {
        return CACHE_KEY_PREFIX + idempotencyKey;
    }
}
