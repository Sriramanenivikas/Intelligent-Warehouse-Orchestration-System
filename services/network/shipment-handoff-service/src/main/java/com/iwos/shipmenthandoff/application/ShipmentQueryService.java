package com.iwos.shipmenthandoff.application;

import com.iwos.shipmenthandoff.api.http.ShipmentResponse;
import com.iwos.shipmenthandoff.domain.shipment.ShipmentNotFoundException;
import com.iwos.shipmenthandoff.domain.shipment.ShipmentStatus;
import com.iwos.shipmenthandoff.infrastructure.persistence.ShipmentResponseMapper;
import com.iwos.shipmenthandoff.infrastructure.persistence.entity.ShipmentEntity;
import com.iwos.shipmenthandoff.infrastructure.persistence.repository.ShipmentRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShipmentQueryService {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentResponseMapper mapper;

    public ShipmentQueryService(ShipmentRepository shipmentRepository, ShipmentResponseMapper mapper) {
        this.shipmentRepository = shipmentRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public ShipmentResponse getById(UUID shipmentId) {
        ShipmentEntity entity = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ShipmentNotFoundException(shipmentId));
        return mapper.toResponse(entity);
    }

    @Transactional(readOnly = true)
    public ShipmentResponse getByFulfillmentOrderId(UUID fulfillmentOrderId) {
        ShipmentEntity entity = shipmentRepository.findByFulfillmentOrderId(fulfillmentOrderId)
                .orElseThrow(() -> new ShipmentNotFoundException(fulfillmentOrderId));
        return mapper.toResponse(entity);
    }

    @Transactional(readOnly = true)
    public ShipmentResponse getByOrderIntentId(UUID orderIntentId) {
        ShipmentEntity entity = shipmentRepository.findByOrderIntentId(orderIntentId)
                .orElseThrow(() -> new ShipmentNotFoundException(orderIntentId));
        return mapper.toResponse(entity);
    }

    @Transactional(readOnly = true)
    public ShipmentResponse getByAwbNumber(String awbNumber) {
        ShipmentEntity entity = shipmentRepository.findByAwbNumber(awbNumber)
                .orElseThrow(() -> new ShipmentNotFoundException(UUID.randomUUID())); // AWB lookup
        return mapper.toResponse(entity);
    }

    @Transactional(readOnly = true)
    public List<ShipmentResponse> listByStatus(ShipmentStatus status, int limit) {
        PageRequest page = PageRequest.of(0, limit);
        return shipmentRepository.findByStatusOrderByCreatedAtDesc(status, page)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ShipmentResponse> listByOriginAndStatus(String originNodeId, ShipmentStatus status, int limit) {
        PageRequest page = PageRequest.of(0, limit);
        return shipmentRepository.findByOriginNodeIdAndStatusOrderByCreatedAtDesc(originNodeId, status, page)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}
