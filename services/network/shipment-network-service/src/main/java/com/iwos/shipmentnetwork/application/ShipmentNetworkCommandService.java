package com.iwos.shipmentnetwork.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwos.shipmentnetwork.api.http.NetworkShipmentResponse;
import com.iwos.shipmentnetwork.api.http.ScanRequest;
import com.iwos.shipmentnetwork.domain.network.NetworkShipmentAlreadyExistsException;
import com.iwos.shipmentnetwork.domain.network.NetworkShipmentNotFoundException;
import com.iwos.shipmentnetwork.domain.network.NetworkShipmentStateException;
import com.iwos.shipmentnetwork.domain.network.NetworkShipmentStatus;
import com.iwos.shipmentnetwork.domain.network.ScanType;
import com.iwos.shipmentnetwork.infrastructure.observability.ShipmentNetworkMetrics;
import com.iwos.shipmentnetwork.infrastructure.persistence.NetworkShipmentResponseMapper;
import com.iwos.shipmentnetwork.infrastructure.persistence.entity.NetworkOutboxEventEntity;
import com.iwos.shipmentnetwork.infrastructure.persistence.entity.NetworkShipmentEntity;
import com.iwos.shipmentnetwork.infrastructure.persistence.entity.ScanEventEntity;
import com.iwos.shipmentnetwork.infrastructure.persistence.repository.NetworkOutboxEventRepository;
import com.iwos.shipmentnetwork.infrastructure.persistence.repository.NetworkShipmentRepository;
import com.iwos.shipmentnetwork.infrastructure.persistence.repository.ScanEventRepository;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShipmentNetworkCommandService {

    private final NetworkShipmentRepository shipmentRepository;
    private final ScanEventRepository scanRepository;
    private final NetworkOutboxEventRepository outboxRepository;
    private final NetworkShipmentResponseMapper mapper;
    private final ShipmentNetworkMetrics metrics;
    private final ObjectMapper objectMapper;

    public ShipmentNetworkCommandService(
            NetworkShipmentRepository shipmentRepository,
            ScanEventRepository scanRepository,
            NetworkOutboxEventRepository outboxRepository,
            NetworkShipmentResponseMapper mapper,
            ShipmentNetworkMetrics metrics,
            ObjectMapper objectMapper
    ) {
        this.shipmentRepository = shipmentRepository;
        this.scanRepository = scanRepository;
        this.outboxRepository = outboxRepository;
        this.mapper = mapper;
        this.metrics = metrics;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public NetworkShipmentResponse createFromShipmentCreatedEvent(Map<String, Object> event) {
        UUID shipmentId = UUID.fromString(required(event, "shipmentId"));
        return shipmentRepository.findByShipmentId(shipmentId)
                .map(this::toResponse)
                .orElseGet(() -> createNetworkShipment(event, shipmentId));
    }

    @Transactional
    public NetworkShipmentResponse recordScan(UUID shipmentId, ScanRequest request) {
        NetworkShipmentEntity shipment = shipmentRepository.findByShipmentId(shipmentId)
                .orElseThrow(() -> new NetworkShipmentNotFoundException(shipmentId));

        ScanType scanType = request.scanType();
        validateTransition(shipment, scanType);

        Instant occurredAt = request.occurredAt() != null ? request.occurredAt() : Instant.now();
        NetworkShipmentStatus newStatus = statusFor(scanType);

        shipment.setStatus(newStatus);
        shipment.setLastScanType(scanType);
        shipment.setLastScannedAt(occurredAt);
        shipment.setUpdatedAt(Instant.now());
        if (request.nodeId() != null && !request.nodeId().isBlank()) {
            shipment.setCurrentNodeId(request.nodeId());
        }
        if (request.facilityCode() != null && !request.facilityCode().isBlank()) {
            shipment.setCurrentFacilityCode(request.facilityCode());
        }

        ScanEventEntity scan = createScanEntity(shipment, scanType, request.nodeId(), request.facilityCode(), request.notes(), occurredAt);
        shipmentRepository.save(shipment);
        scanRepository.save(scan);
        createOutboxEvent(shipment, scan, eventTypeFor(scanType));
        metrics.incrementScanEventsRecorded();

        return toResponse(shipment);
    }

    private NetworkShipmentResponse createNetworkShipment(Map<String, Object> event, UUID shipmentId) {
        if (shipmentRepository.existsByShipmentId(shipmentId)) {
            throw new NetworkShipmentAlreadyExistsException(shipmentId);
        }

        Instant now = Instant.now();
        NetworkShipmentEntity shipment = new NetworkShipmentEntity();
        shipment.setNetworkShipmentId(UUID.randomUUID());
        shipment.setShipmentId(shipmentId);
        shipment.setFulfillmentOrderId(UUID.fromString(required(event, "fulfillmentOrderId")));
        shipment.setOrderIntentId(UUID.fromString(required(event, "orderIntentId")));
        shipment.setAwbNumber(required(event, "awbNumber"));
        shipment.setCarrierCode(required(event, "carrierCode"));
        shipment.setCustomerId((String) event.get("customerId"));
        shipment.setOriginNodeId((String) event.get("originNodeId"));
        shipment.setCurrentNodeId((String) event.get("originNodeId"));
        shipment.setCurrentFacilityCode((String) event.getOrDefault("originNodeId", "ORIGIN"));
        shipment.setStatus(NetworkShipmentStatus.CREATED);
        shipment.setLastScanType(ScanType.CREATED);
        shipment.setLastScannedAt(now);
        shipment.setCreatedAt(now);
        shipment.setUpdatedAt(now);

        ScanEventEntity scan = createScanEntity(shipment, ScanType.CREATED, shipment.getCurrentNodeId(),
                shipment.getCurrentFacilityCode(), "shipment handoff created", now);
        shipmentRepository.save(shipment);
        scanRepository.save(scan);
        createOutboxEvent(shipment, scan, "shipment-network.shipment-created.v1");
        metrics.incrementNetworkShipmentsCreated();
        metrics.incrementScanEventsRecorded();
        return toResponse(shipment);
    }

    private ScanEventEntity createScanEntity(
            NetworkShipmentEntity shipment,
            ScanType scanType,
            String nodeId,
            String facilityCode,
            String notes,
            Instant occurredAt
    ) {
        ScanEventEntity scan = new ScanEventEntity();
        scan.setScanEventId(UUID.randomUUID());
        scan.setNetworkShipment(shipment);
        scan.setShipmentId(shipment.getShipmentId());
        scan.setAwbNumber(shipment.getAwbNumber());
        scan.setScanType(scanType);
        scan.setNodeId(nodeId);
        scan.setFacilityCode(facilityCode);
        scan.setNotes(notes);
        scan.setOccurredAt(occurredAt);
        scan.setCreatedAt(Instant.now());
        return scan;
    }

    private void validateTransition(NetworkShipmentEntity shipment, ScanType scanType) {
        if (shipment.getStatus() == NetworkShipmentStatus.DELIVERED && scanType != ScanType.DELIVERED) {
            throw new NetworkShipmentStateException(shipment.getShipmentId(), shipment.getStatus(), scanType);
        }
    }

    private NetworkShipmentStatus statusFor(ScanType scanType) {
        return switch (scanType) {
            case CREATED -> NetworkShipmentStatus.CREATED;
            case MANIFESTED -> NetworkShipmentStatus.MANIFESTED;
            case HUB_RECEIVED -> NetworkShipmentStatus.HUB_RECEIVED;
            case SORTED -> NetworkShipmentStatus.SORTED;
            case OUT_FOR_DELIVERY -> NetworkShipmentStatus.OUT_FOR_DELIVERY;
            case DELIVERED -> NetworkShipmentStatus.DELIVERED;
            case EXCEPTION -> NetworkShipmentStatus.EXCEPTION;
        };
    }

    private String eventTypeFor(ScanType scanType) {
        return switch (scanType) {
            case CREATED -> "shipment-network.shipment-created.v1";
            case MANIFESTED -> "shipment-network.shipment-manifested.v1";
            case HUB_RECEIVED -> "shipment-network.hub-received.v1";
            case SORTED -> "shipment-network.sorted.v1";
            case OUT_FOR_DELIVERY -> "shipment-network.out-for-delivery.v1";
            case DELIVERED -> "shipment-network.delivered.v1";
            case EXCEPTION -> "shipment-network.exception.v1";
        };
    }

    private void createOutboxEvent(NetworkShipmentEntity shipment, ScanEventEntity scan, String eventType) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("eventType", eventType);
            payload.put("shipmentId", shipment.getShipmentId().toString());
            payload.put("networkShipmentId", shipment.getNetworkShipmentId().toString());
            payload.put("fulfillmentOrderId", shipment.getFulfillmentOrderId().toString());
            payload.put("orderIntentId", shipment.getOrderIntentId().toString());
            payload.put("awbNumber", shipment.getAwbNumber());
            payload.put("carrierCode", shipment.getCarrierCode());
            payload.put("status", shipment.getStatus().name());
            payload.put("scanEventId", scan.getScanEventId().toString());
            payload.put("scanType", scan.getScanType().name());
            payload.put("nodeId", scan.getNodeId());
            payload.put("facilityCode", scan.getFacilityCode());
            payload.put("occurredAt", scan.getOccurredAt().toString());

            Instant now = Instant.now();
            NetworkOutboxEventEntity outbox = new NetworkOutboxEventEntity();
            outbox.setOutboxEventId(UUID.randomUUID());
            outbox.setAggregateType("NETWORK_SHIPMENT");
            outbox.setAggregateId(shipment.getNetworkShipmentId());
            outbox.setEventType(eventType);
            outbox.setStatus("PENDING");
            outbox.setAttempts(0);
            outbox.setPayload(objectMapper.writeValueAsString(payload));
            outbox.setCreatedAt(now);
            outbox.setUpdatedAt(now);
            outboxRepository.save(outbox);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create shipment-network outbox event", e);
        }
    }

    private NetworkShipmentResponse toResponse(NetworkShipmentEntity shipment) {
        return mapper.toResponse(shipment, scanRepository.findByShipmentIdOrderByOccurredAtAsc(shipment.getShipmentId()));
    }

    private String required(Map<String, Object> event, String key) {
        Object value = event.get(key);
        if (value == null || value.toString().isBlank()) {
            throw new IllegalArgumentException("Missing required event field: " + key);
        }
        return value.toString();
    }
}
