package com.iwos.noderegistry.api.http;

import com.iwos.noderegistry.infrastructure.persistence.entity.NodeEntity;
import java.time.Instant;
import java.util.UUID;

public record NodeResponse(
        String nodeId,
        String nodeCode,
        String displayName,
        String nodeType,
        String city,
        String state,
        String country,
        String postalCode,
        String timezone,
        int priority,
        boolean supportsExpress,
        boolean supportsParcel,
        boolean active,
        UUID externalReferenceId,
        Instant createdAt,
        Instant updatedAt
) {

    public static NodeResponse from(NodeEntity entity) {
        return new NodeResponse(
                entity.getNodeId(),
                entity.getNodeCode(),
                entity.getDisplayName(),
                entity.getNodeType(),
                entity.getCity(),
                entity.getState(),
                entity.getCountry(),
                entity.getPostalCode(),
                entity.getTimezone(),
                entity.getPriority(),
                entity.isSupportsExpress(),
                entity.isSupportsParcel(),
                entity.isActive(),
                entity.getExternalReferenceId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
