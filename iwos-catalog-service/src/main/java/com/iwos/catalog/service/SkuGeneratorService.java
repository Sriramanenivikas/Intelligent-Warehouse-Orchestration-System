package com.iwos.catalog.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

@Service
public class SkuGeneratorService {

    private final AtomicLong counter = new AtomicLong(System.currentTimeMillis() % 100000);

    public String generateSku(String categorySlug) {
        String prefix = categorySlug.length() >= 3
                ? categorySlug.substring(0, 3).toUpperCase()
                : categorySlug.toUpperCase();
        long seq = counter.incrementAndGet();
        return String.format("IWOS-%s-%06d", prefix, seq);
    }
}
