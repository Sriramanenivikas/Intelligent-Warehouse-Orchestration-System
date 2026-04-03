package com.iwos.promiseallocation.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "node_profiles", schema = "promise_allocation")
public class NodeProfileEntity {

    @Id
    @Column(name = "node_id", nullable = false, length = 64)
    private String nodeId;

    @Column(name = "node_name", nullable = false, length = 128)
    private String nodeName;

    @Column(name = "city", nullable = false, length = 64)
    private String city;

    @Column(name = "state", nullable = false, length = 64)
    private String state;

    @Column(name = "country", nullable = false, length = 2)
    private String country;

    @Column(name = "postal_code", nullable = false, length = 16)
    private String postalCode;

    @Column(name = "priority", nullable = false)
    private int priority;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public String getNodeId() {
        return nodeId;
    }

    public String getNodeName() {
        return nodeName;
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

    public int getPriority() {
        return priority;
    }

    public boolean isActive() {
        return active;
    }
}
