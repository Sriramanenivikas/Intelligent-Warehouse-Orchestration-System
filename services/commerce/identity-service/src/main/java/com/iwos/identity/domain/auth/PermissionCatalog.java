package com.iwos.identity.domain.auth;

import java.util.List;
import java.util.Map;

public final class PermissionCatalog {

    private static final Map<Role, List<String>> PERMISSIONS = Map.of(
            Role.STORE_MANAGER, List.of(
                    "order:read",
                    "inventory:read",
                    "inventory:update",
                    "task:read",
                    "task:claim",
                    "task:complete",
                    "shipment:read"
            ),
            Role.FC_OPERATOR, List.of(
                    "inventory:read",
                    "inventory:update",
                    "task:read",
                    "task:claim",
                    "task:complete",
                    "shipment:read"
            ),
            Role.OPS_ADMIN, List.of(
                    "order:read",
                    "inventory:read",
                    "inventory:update",
                    "shipment:read",
                    "shipment:update",
                    "task:read",
                    "task:claim",
                    "task:complete",
                    "notification:read",
                    "scan-event:read"
            ),
            Role.PLANNER_ANALYST, List.of(
                    "forecast:read",
                    "forecast:write",
                    "replenishment:read",
                    "replenishment:write",
                    "inventory:read",
                    "shipment:read"
            ),
            Role.PLATFORM_ADMIN, List.of(
                    "platform:*"
            )
    );

    private PermissionCatalog() {
    }

    public static List<String> permissionsFor(Role role) {
        return PERMISSIONS.getOrDefault(role, List.of());
    }
}
