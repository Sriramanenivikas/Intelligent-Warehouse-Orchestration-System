package com.iwos.notification.infrastructure.persistence.entity;

import com.iwos.notification.domain.notification.NotificationAudience;
import com.iwos.notification.domain.notification.NotificationChannel;
import com.iwos.notification.domain.notification.NotificationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications", schema = "notification")
public class NotificationEntity {

    @Id
    @Column(name = "notification_id", nullable = false, updatable = false)
    private UUID notificationId;

    @Column(name = "external_ref_id", nullable = false, unique = true)
    private UUID externalRefId;

    @Column(name = "shipment_id", nullable = false)
    private UUID shipmentId;

    @Column(name = "order_intent_id", nullable = false)
    private UUID orderIntentId;

    @Column(name = "tracked_shipment_id")
    private UUID trackedShipmentId;

    @Column(name = "awb_number", nullable = false, length = 64)
    private String awbNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "audience", nullable = false, length = 16)
    private NotificationAudience audience;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 16)
    private NotificationChannel channel;

    @Column(name = "template_code", nullable = false, length = 64)
    private String templateCode;

    @Column(name = "event_type", nullable = false, length = 128)
    private String eventType;

    @Column(name = "scan_type", nullable = false, length = 32)
    private String scanType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private NotificationStatus status;

    @Column(name = "title", nullable = false, length = 160)
    private String title;

    @Column(name = "message", nullable = false, length = 512)
    private String message;

    @Column(name = "metadata_json", nullable = false, columnDefinition = "text")
    private String metadataJson;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    public UUID getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(UUID notificationId) {
        this.notificationId = notificationId;
    }

    public UUID getExternalRefId() {
        return externalRefId;
    }

    public void setExternalRefId(UUID externalRefId) {
        this.externalRefId = externalRefId;
    }

    public UUID getShipmentId() {
        return shipmentId;
    }

    public void setShipmentId(UUID shipmentId) {
        this.shipmentId = shipmentId;
    }

    public UUID getOrderIntentId() {
        return orderIntentId;
    }

    public void setOrderIntentId(UUID orderIntentId) {
        this.orderIntentId = orderIntentId;
    }

    public UUID getTrackedShipmentId() {
        return trackedShipmentId;
    }

    public void setTrackedShipmentId(UUID trackedShipmentId) {
        this.trackedShipmentId = trackedShipmentId;
    }

    public String getAwbNumber() {
        return awbNumber;
    }

    public void setAwbNumber(String awbNumber) {
        this.awbNumber = awbNumber;
    }

    public NotificationAudience getAudience() {
        return audience;
    }

    public void setAudience(NotificationAudience audience) {
        this.audience = audience;
    }

    public NotificationChannel getChannel() {
        return channel;
    }

    public void setChannel(NotificationChannel channel) {
        this.channel = channel;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getScanType() {
        return scanType;
    }

    public void setScanType(String scanType) {
        this.scanType = scanType;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public void setStatus(NotificationStatus status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMetadataJson() {
        return metadataJson;
    }

    public void setMetadataJson(String metadataJson) {
        this.metadataJson = metadataJson;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(Instant deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}
