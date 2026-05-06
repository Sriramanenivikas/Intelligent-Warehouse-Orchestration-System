package com.iwos.noderegistry.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "nodes", schema = "node_registry")
public class NodeEntity {

    @Id
    @Column(name = "node_id", nullable = false, length = 64)
    private String nodeId;

    @Column(name = "node_code", nullable = false, length = 64, unique = true)
    private String nodeCode;

    @Column(name = "display_name", nullable = false, length = 128)
    private String displayName;

    @Column(name = "node_type", nullable = false, length = 32)
    private String nodeType;

    @Column(name = "city", nullable = false, length = 64)
    private String city;

    @Column(name = "state", nullable = false, length = 64)
    private String state;

    @Column(name = "country", nullable = false, length = 2)
    private String country;

    @Column(name = "postal_code", nullable = false, length = 16)
    private String postalCode;

    @Column(name = "timezone", nullable = false, length = 64)
    private String timezone;

    @Column(name = "priority", nullable = false)
    private Integer priority;

    @Column(name = "supports_express", nullable = false)
    private boolean supportsExpress;

    @Column(name = "supports_parcel", nullable = false)
    private boolean supportsParcel;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "external_reference_id")
    private UUID externalReferenceId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public String getNodeId() {
        return nodeId;
    }

    public String getNodeCode() {
        return nodeCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getNodeType() {
        return nodeType;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getCountry() {
        return country;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getTimezone() {
        return timezone;
    }

    public Integer getPriority() {
        return priority;
    }

    public boolean isSupportsExpress() {
        return supportsExpress;
    }

    public boolean isSupportsParcel() {
        return supportsParcel;
    }

    public boolean isActive() {
        return active;
    }

    public UUID getExternalReferenceId() {
        return externalReferenceId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
