package com.iwos.review.service;

import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class ModerationService {

    private static final Set<String> BLOCKED_WORDS = Set.of(
            "spam", "fake", "scam"  // Real implementation would use ML-based moderation
    );

    public boolean containsProfanity(String content) {
        if (content == null) return false;
        String lower = content.toLowerCase();
        return BLOCKED_WORDS.stream().anyMatch(lower::contains);
    }
}
