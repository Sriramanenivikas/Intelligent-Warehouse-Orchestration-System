package com.iwos.identity.domain.auth;

import java.util.List;

public record AuthenticatedPrincipal(
        String subject,
        String username,
        Role role,
        String region,
        String orgId,
        List<String> nodeIds
) {
}
