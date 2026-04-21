package com.iwos.scanevent.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwos.scanevent.domain.scan.ScanMilestone;
import com.iwos.scanevent.domain.scan.TrackingStatus;
import com.iwos.scanevent.infrastructure.observability.ScanEventMetrics;
import com.iwos.scanevent.infrastructure.persistence.entity.NormalizedScanEventEntity;
import com.iwos.scanevent.infrastructure.persistence.entity.ScanEventOutboxEventEntity;
import com.iwos.scanevent.infrastructure.persistence.entity.TrackedShipmentEntity;
import com.iwos.scanevent.infrastructure.persistence.repository.NormalizedScanEventRepository;
import com.iwos.scanevent.infrastructure.persistence.repository.ScanEventOutboxEventRepository;
import com.iwos.scanevent.infrastructure.persistence.repository.TrackedShipmentRepository;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScanEventCommandService {

    private final TrackedShipmentRepository trackedShipmentRepository;
    private final NormalizedScanEventRepository normalizedScanEventRepository;
    private final ScanEventOutboxEventRepository outboxRepository;
    private final ScanEventMetrics metrics;
    private final ObjectMapper objectMapper;

    public ScanEventCommandService(
            TrackedShipmentRepository trackedShipmentRepository,
            NormalizedScanEventRepository normalizedScanEventRepository,
            ScanEventOutboxEventRepository outboxRepository,
            ScanEventMetrics metrics,
            ObjectMapper objectMapper
    ) {
        this.trackedShipmentRepository = trackedShipmentRepository;
        this.normalizedScanEventRepository = normalizedScanEventRepository;
        this.outboxRepository = outboxRepository;
        this.metrics = metrics;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void consumeShipmentNetworkEvent(Map<String, Object> event) {
        UUID shipmentId = UUID.fromString(required(event, "shipmentId"));
        UUID scanEventId = UUID.fromString(required(event, "scanEventId"));

        if (normalizedScanEventRepository.existsByScanEventId(scanEventId)) {
            metrics.incrementInboundEventsSkipped();
            return;
        }

        Instant now = Instant.now();
        TrackedShipmentEntity trackedShipment = trackedShipmentRepository.findByShipmentId(shipmentId)
                .orElseGet(() -> createTrackedShipment(event, shipmentId, now));

        updateTrackedShipment(trackedShipment, event, now);
        NormalizedScanEventEntity normalizedEvent = createNormalizedEvent(trackedShipment, event, scanEventId, now);

        trackedShipmentRepository.save(trackedShipment);
        normalizedScanEventRepository.save(normalizedEvent);
        createOutboxEvent(trackedShipment, normalizedEvent);

        metrics.incrementScanEventsNormalized();
        metrics.incrementInboundEventsProcessed();
    }

    private TrackedShipmentEntity createTrackedShipment(Map<String, Object> event, UUID shipmentId, Instant now) {
        TrackedShipmentEntity shipment = new TrackedShipmentEntity();
        shipment.setTrackedShipmentId(UUID.randomUUID());
        shipment.setShipmentId(shipmentId);
        shipment.setCreatedAt(now);
        shipment.setUpdatedAt(now);
        metrics.incrementTrackedShipmentsCreated();
        updateTrackedShipment(shipment, event, now);
        return shipment;
    }

    private void updateTrackedShipment(TrackedShipmentEntity trackedShipment, Map<String, Object> event, Instant now) {
        trackedShipment.setNetworkShipmentId(optionalUuid(event, "networkShipmentId").orElse(trackedShipment.getNetworkShipmentId()));
        trackedShipment.setFulfillmentOrderId(UUID.fromString(required(event, "fulfillmentOrderId")));
        trackedShipment.setOrderIntentId(UUID.fromString(required(event, "orderIntentId")));
        trackedShipment.setAwbNumber(required(event, "awbNumber"));
        trackedShipment.setCarrierCode(required(event, "carrierCode"));
        trackedShipment.setCustomerId(optionalString(event, "customerId").orElse(trackedShipment.getCustomerId()));
        trackedShipment.setCurrentStatus(statusFrom(event));
        trackedShipment.setLastScanType(scanMilestoneFrom(event));
        trackedShipment.setLastScannedAt(occurredAt(event));
        trackedShipment.setUpdatedAt(now);
    }

    private NormalizedScanEventEntity createNormalizedEvent(
            TrackedShipmentEntity trackedShipment,
            Map<String, Object> event,
            UUID scanEventId,
            Instant now
    ) {
        NormalizedScanEventEntity normalizedEvent = new NormalizedScanEventEntity();
        normalizedEvent.setNormalizedScanEventId(UUID.randomUUID());
        normalizedEvent.setTrackedShipment(trackedShipment);
        normalizedEvent.setScanEventId(scanEventId);
        normalizedEvent.setShipmentId(trackedShipment.getShipmentId());
        normalizedEvent.setOrderIntentId(trackedShipment.getOrderIntentId());
        normalizedEvent.setAwbNumber(trackedShipment.getAwbNumber());
        normalizedEvent.setSourceEventType(required(event, "eventType"));
        normalizedEvent.setScanType(scanMilestoneFrom(event));
        normalizedEvent.setStatusAfterEvent(statusFrom(event));
        normalizedEvent.setNodeId(optionalString(event, "nodeId").orElse(null));
        normalizedEvent.setFacilityCode(optionalString(event, "facilityCode").orElse(null));
        normalizedEvent.setNotes(optionalString(event, "notes").orElse(null));
        normalizedEvent.setOccurredAt(occurredAt(event));
        normalizedEvent.setIngestedAt(now);
        return normalizedEvent;
    }

    private void createOutboxEvent(TrackedShipmentEntity trackedShipment, NormalizedScanEventEntity normalizedEvent) {
        try {
            Instant now = Instant.now();
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("eventType", "scan.milestone-recorded.v1");
            payload.put("shipmentId", trackedShipment.getShipmentId().toString());
            payload.put("trackedShipmentId", trackedShipment.getTrackedShipmentId().toString());
            payload.put("networkShipmentId", trackedShipment.getNetworkShipmentId() != null ? trackedShipment.getNetworkShipmentId().toString() : null);
            payload.put("fulfillmentOrderId", trackedShipment.getFulfillmentOrderId().toString());
            payload.put("orderIntentId", trackedShipment.getOrderIntentId().toString());
            payload.put("awbNumber", trackedShipment.getAwbNumber());
            payload.put("carrierCode", trackedShipment.getCarrierCode());
            payload.put("sourceEventType", normalizedEvent.getSourceEventType());
            payload.put("scanEventId", normalizedEvent.getScanEventId().toString());
            payload.put("scanType", normalizedEvent.getScanType().name());
            payload.put("status", normalizedEvent.getStatusAfterEvent().name());
            payload.put("nodeId", normalizedEvent.getNodeId());
            payload.put("facilityCode", normalizedEvent.getFacilityCode());
            payload.put("notes", normalizedEvent.getNotes());
            payload.put("occurredAt", normalizedEvent.getOccurredAt().toString());

            ScanEventOutboxEventEntity outbox = new ScanEventOutboxEventEntity();
            outbox.setOutboxEventId(UUID.randomUUID());
            outbox.setAggregateType("TRACKED_SHIPMENT");
            outbox.setAggregateId(trackedShipment.getTrackedShipmentId());
            outbox.setEventType("scan.milestone-recorded.v1");
            outbox.setStatus("PENDING");
            outbox.setAttempts(0);
            outbox.setPayload(objectMapper.writeValueAsString(payload));
            outbox.setCreatedAt(now);
            outbox.setUpdatedAt(now);
            outboxRepository.save(outbox);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create scan-event outbox event", e);
        }
    }

    private TrackingStatus statusFrom(Map<String, Object> event) {
        return optionalString(event, "status")
                .map(TrackingStatus::valueOf)
                .orElseGet(() -> TrackingStatus.valueOf(required(event, "scanType")));
    }

    private ScanMilestone scanMilestoneFrom(Map<String, Object> event) {
        return ScanMilestone.valueOf(required(event, "scanType"));
    }

    private Instant occurredAt(Map<String, Object> event) {
        return Instant.parse(required(event, "occurredAt"));
    }

    private String required(Map<String, Object> event, String key) {
        Object value = event.get(key);
        if (value == null || value.toString().isBlank()) {
            throw new IllegalArgumentException("Missing required event field: " + key);
        }
        return value.toString();
    }

    private Optional<String> optionalString(Map<String, Object> event, String key) {
        Object value = event.get(key);
        return value == null || value.toString().isBlank() ? Optional.empty() : Optional.of(value.toString());
    }

    private Optional<UUID> optionalUuid(Map<String, Object> event, String key) {
        return optionalString(event, key).map(UUID::fromString);
    }
}
