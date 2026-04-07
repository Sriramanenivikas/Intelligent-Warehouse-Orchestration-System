package com.iwos.shipmenthandoff.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwos.shipmenthandoff.api.http.ShipmentResponse;
import com.iwos.shipmenthandoff.domain.shipment.CarrierCode;
import com.iwos.shipmenthandoff.domain.shipment.ShipmentAlreadyExistsException;
import com.iwos.shipmenthandoff.domain.shipment.ShipmentNotFoundException;
import com.iwos.shipmenthandoff.domain.shipment.ShipmentStateException;
import com.iwos.shipmenthandoff.domain.shipment.ShipmentStatus;
import com.iwos.shipmenthandoff.infrastructure.observability.ShipmentMetrics;
import com.iwos.shipmenthandoff.infrastructure.persistence.ShipmentResponseMapper;
import com.iwos.shipmenthandoff.infrastructure.persistence.entity.ShipmentEntity;
import com.iwos.shipmenthandoff.infrastructure.persistence.entity.ShipmentOutboxEventEntity;
import com.iwos.shipmenthandoff.infrastructure.persistence.repository.ShipmentOutboxEventRepository;
import com.iwos.shipmenthandoff.infrastructure.persistence.repository.ShipmentRepository;
import com.iwos.shipmenthandoff.infrastructure.taskexecution.FulfillmentOrderResponse;
import com.iwos.shipmenthandoff.infrastructure.taskexecution.OrderIntakeClient;
import com.iwos.shipmenthandoff.infrastructure.taskexecution.OrderIntentResponse;
import com.iwos.shipmenthandoff.infrastructure.taskexecution.WarehouseOrchestratorClient;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShipmentCommandService {

    private static final Logger log = LoggerFactory.getLogger(ShipmentCommandService.class);

    private final ShipmentRepository shipmentRepository;
    private final ShipmentOutboxEventRepository outboxRepository;
    private final WarehouseOrchestratorClient warehouseClient;
    private final OrderIntakeClient orderIntakeClient;
    private final ShipmentResponseMapper mapper;
    private final ShipmentMetrics metrics;
    private final ObjectMapper objectMapper;

    public ShipmentCommandService(
            ShipmentRepository shipmentRepository,
            ShipmentOutboxEventRepository outboxRepository,
            WarehouseOrchestratorClient warehouseClient,
            OrderIntakeClient orderIntakeClient,
            ShipmentResponseMapper mapper,
            ShipmentMetrics metrics,
            ObjectMapper objectMapper
    ) {
        this.shipmentRepository = shipmentRepository;
        this.outboxRepository = outboxRepository;
        this.warehouseClient = warehouseClient;
        this.orderIntakeClient = orderIntakeClient;
        this.mapper = mapper;
        this.metrics = metrics;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ShipmentResponse createShipment(UUID fulfillmentOrderId, CarrierCode carrierCode, Integer weightGrams, Integer packageCount) {
        // Check if shipment already exists
        if (shipmentRepository.existsByFulfillmentOrderId(fulfillmentOrderId)) {
            throw new ShipmentAlreadyExistsException(fulfillmentOrderId);
        }

        // Fetch fulfillment order
        FulfillmentOrderResponse fulfillment = warehouseClient.getFulfillmentOrder(fulfillmentOrderId)
                .orElseThrow(() -> new ShipmentNotFoundException(fulfillmentOrderId));

        // Fetch order intent for delivery address
        OrderIntentResponse orderIntent = orderIntakeClient.getOrderIntent(fulfillment.orderIntentId())
                .orElseThrow(() -> new ShipmentNotFoundException(fulfillment.orderIntentId()));

        // Generate AWB
        String awbNumber = generateAwbNumber(carrierCode);

        // Create shipment
        Instant now = Instant.now();
        ShipmentEntity shipment = new ShipmentEntity();
        shipment.setShipmentId(UUID.randomUUID());
        shipment.setFulfillmentOrderId(fulfillmentOrderId);
        shipment.setOrderIntentId(fulfillment.orderIntentId());
        shipment.setWorkflowId(fulfillment.workflowId());
        shipment.setCustomerId(fulfillment.customerId());
        shipment.setAwbNumber(awbNumber);
        shipment.setCarrierCode(carrierCode);
        shipment.setStatus(ShipmentStatus.CREATED);
        shipment.setOriginNodeId(fulfillment.fulfillmentNodeId());

        // Set destination from order intent
        OrderIntentResponse.DeliveryAddress addr = orderIntent.deliveryAddress();
        shipment.setDestinationName(addr.name());
        shipment.setDestinationPhone(addr.phone());
        shipment.setDestinationLine1(addr.line1());
        shipment.setDestinationLine2(addr.line2());
        shipment.setDestinationCity(addr.city());
        shipment.setDestinationState(addr.state());
        shipment.setDestinationPostalCode(addr.postalCode());
        shipment.setDestinationCountry(addr.country());

        shipment.setWeightGrams(weightGrams != null ? weightGrams : 500);
        shipment.setPackageCount(packageCount != null ? packageCount : 1);
        shipment.setEstimatedDeliveryAt(now.plus(3, ChronoUnit.DAYS));
        shipment.setCreatedAt(now);
        shipment.setUpdatedAt(now);

        shipment = shipmentRepository.save(shipment);
        metrics.incrementShipmentsCreated();

        // Create outbox event
        createOutboxEvent(shipment, "shipment-handoff.shipment-created.v1");

        log.info("Shipment created: shipmentId={}, awbNumber={}, fulfillmentOrderId={}",
                shipment.getShipmentId(), awbNumber, fulfillmentOrderId);

        return mapper.toResponse(shipment);
    }

    @Transactional
    public ShipmentResponse manifestShipment(UUID shipmentId) {
        ShipmentEntity shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ShipmentNotFoundException(shipmentId));

        if (shipment.getStatus() == ShipmentStatus.MANIFESTED) {
            return mapper.toResponse(shipment);
        }

        if (shipment.getStatus() != ShipmentStatus.CREATED) {
            throw new ShipmentStateException(shipmentId, shipment.getStatus(), "manifest");
        }

        Instant now = Instant.now();
        shipment.setStatus(ShipmentStatus.MANIFESTED);
        shipment.setManifestedAt(now);
        shipment.setUpdatedAt(now);

        shipment = shipmentRepository.save(shipment);
        metrics.incrementShipmentsManifested();

        createOutboxEvent(shipment, "shipment-handoff.shipment-manifested.v1");

        log.info("Shipment manifested: shipmentId={}, awbNumber={}", shipmentId, shipment.getAwbNumber());
        return mapper.toResponse(shipment);
    }

    @Transactional
    public ShipmentResponse dispatchShipment(UUID shipmentId) {
        ShipmentEntity shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ShipmentNotFoundException(shipmentId));

        if (shipment.getStatus() == ShipmentStatus.DISPATCHED) {
            return mapper.toResponse(shipment);
        }

        if (shipment.getStatus() != ShipmentStatus.MANIFESTED) {
            throw new ShipmentStateException(shipmentId, shipment.getStatus(), "dispatch");
        }

        Instant now = Instant.now();
        shipment.setStatus(ShipmentStatus.DISPATCHED);
        shipment.setDispatchedAt(now);
        shipment.setUpdatedAt(now);

        shipment = shipmentRepository.save(shipment);
        metrics.incrementShipmentsDispatched();

        createOutboxEvent(shipment, "shipment-handoff.shipment-dispatched.v1");

        log.info("Shipment dispatched: shipmentId={}, awbNumber={}", shipmentId, shipment.getAwbNumber());
        return mapper.toResponse(shipment);
    }

    @Transactional
    public ShipmentResponse markDelivered(UUID shipmentId) {
        ShipmentEntity shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ShipmentNotFoundException(shipmentId));

        if (shipment.getStatus() == ShipmentStatus.DELIVERED) {
            return mapper.toResponse(shipment);
        }

        Instant now = Instant.now();
        shipment.setStatus(ShipmentStatus.DELIVERED);
        shipment.setDeliveredAt(now);
        shipment.setUpdatedAt(now);

        shipment = shipmentRepository.save(shipment);
        metrics.incrementShipmentsDelivered();

        createOutboxEvent(shipment, "shipment-handoff.shipment-delivered.v1");

        log.info("Shipment delivered: shipmentId={}, awbNumber={}", shipmentId, shipment.getAwbNumber());
        return mapper.toResponse(shipment);
    }

    private String generateAwbNumber(CarrierCode carrier) {
        String prefix = switch (carrier) {
            case INTERNAL -> "INT";
            case DELHIVERY -> "DEL";
            case FEDEX -> "FDX";
            case BLUEDART -> "BLU";
            case ECOM_EXPRESS -> "ECM";
        };
        return prefix + "-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void createOutboxEvent(ShipmentEntity shipment, String eventType) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("shipmentId", shipment.getShipmentId().toString());
            payload.put("fulfillmentOrderId", shipment.getFulfillmentOrderId().toString());
            payload.put("orderIntentId", shipment.getOrderIntentId().toString());
            payload.put("awbNumber", shipment.getAwbNumber());
            payload.put("carrierCode", shipment.getCarrierCode().name());
            payload.put("status", shipment.getStatus().name());
            payload.put("originNodeId", shipment.getOriginNodeId());
            payload.put("customerId", shipment.getCustomerId());
            payload.put("occurredAt", Instant.now().toString());
            payload.put("eventType", eventType);
            payload.put("aggregateType", "SHIPMENT");
            payload.put("aggregateId", shipment.getShipmentId().toString());

            Instant now = Instant.now();
            ShipmentOutboxEventEntity outbox = new ShipmentOutboxEventEntity();
            outbox.setOutboxEventId(UUID.randomUUID());
            outbox.setAggregateType("SHIPMENT");
            outbox.setAggregateId(shipment.getShipmentId());
            outbox.setEventType(eventType);
            outbox.setStatus("PENDING");
            outbox.setPayload(objectMapper.writeValueAsString(payload));
            outbox.setCreatedAt(now);
            outbox.setUpdatedAt(now);

            outboxRepository.save(outbox);
        } catch (Exception e) {
            log.error("Failed to create outbox event for shipment: {}", shipment.getShipmentId(), e);
        }
    }
}
