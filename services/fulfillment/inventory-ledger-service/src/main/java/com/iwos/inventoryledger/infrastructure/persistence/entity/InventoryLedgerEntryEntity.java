package com.iwos.inventoryledger.infrastructure.persistence.entity;

import com.iwos.inventoryledger.domain.inventory.InventoryLedgerEntryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "inventory_ledger_entries", schema = "inventory_ledger")
public class InventoryLedgerEntryEntity {

    @Id
    @Column(name = "ledger_entry_id", nullable = false, updatable = false)
    private UUID ledgerEntryId;

    @Column(name = "node_id", nullable = false, length = 64)
    private String nodeId;

    @Column(name = "sku", nullable = false, length = 128)
    private String sku;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false, length = 32)
    private InventoryLedgerEntryType entryType;

    @Column(name = "on_hand_delta", nullable = false)
    private int onHandDelta;

    @Column(name = "reserved_delta", nullable = false)
    private int reservedDelta;

    @Column(name = "reason", length = 128)
    private String reason;

    @Column(name = "reference_type", length = 64)
    private String referenceType;

    @Column(name = "reference_id", length = 128)
    private String referenceId;

    @Column(name = "reservation_id")
    private UUID reservationId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public UUID getLedgerEntryId() {
        return ledgerEntryId;
    }

    public void setLedgerEntryId(UUID ledgerEntryId) {
        this.ledgerEntryId = ledgerEntryId;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public InventoryLedgerEntryType getEntryType() {
        return entryType;
    }

    public void setEntryType(InventoryLedgerEntryType entryType) {
        this.entryType = entryType;
    }

    public int getOnHandDelta() {
        return onHandDelta;
    }

    public void setOnHandDelta(int onHandDelta) {
        this.onHandDelta = onHandDelta;
    }

    public int getReservedDelta() {
        return reservedDelta;
    }

    public void setReservedDelta(int reservedDelta) {
        this.reservedDelta = reservedDelta;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public UUID getReservationId() {
        return reservationId;
    }

    public void setReservationId(UUID reservationId) {
        this.reservationId = reservationId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
