package com.iwos.identity.api.http;

import java.time.Instant;
import java.util.List;

public record TokenResponse(
        String tokenType,
        String accessToken,
        String issuer,
        String audience,
        String subject,
        String username,
        String role,
        List<String> permissions,
        List<String> nodeIds,
        String region,
        String orgId,
        String requestId,
        Instant issuedAt,
        Instant expiresAt
) {
}
