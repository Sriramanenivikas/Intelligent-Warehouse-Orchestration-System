package com.iwos.shipmentnetwork.application;

import com.iwos.shipmentnetwork.api.http.NetworkShipmentResponse;
import com.iwos.shipmentnetwork.domain.network.NetworkShipmentNotFoundException;
import com.iwos.shipmentnetwork.infrastructure.persistence.NetworkShipmentResponseMapper;
import com.iwos.shipmentnetwork.infrastructure.persistence.entity.NetworkShipmentEntity;
import com.iwos.shipmentnetwork.infrastructure.persistence.repository.NetworkShipmentRepository;
import com.iwos.shipmentnetwork.infrastructure.persistence.repository.ScanEventRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShipmentNetworkQueryService {

    private final NetworkShipmentRepository shipmentRepository;
    private final ScanEventRepository scanRepository;
    private final NetworkShipmentResponseMapper mapper;

    public ShipmentNetworkQueryService(
            NetworkShipmentRepository shipmentRepository,
            ScanEventRepository scanRepository,
            NetworkShipmentResponseMapper mapper
    ) {
        this.shipmentRepository = shipmentRepository;
        this.scanRepository = scanRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public NetworkShipmentResponse getByShipmentId(UUID shipmentId) {
        return toResponse(shipmentRepository.findByShipmentId(shipmentId)
                .orElseThrow(() -> new NetworkShipmentNotFoundException(shipmentId)));
    }

    @Transactional(readOnly = true)
    public NetworkShipmentResponse getByOrderIntentId(UUID orderIntentId) {
        return toResponse(shipmentRepository.findByOrderIntentId(orderIntentId)
                .orElseThrow(() -> new NetworkShipmentNotFoundException(orderIntentId)));
    }

    @Transactional(readOnly = true)
    public NetworkShipmentResponse getByAwbNumber(String awbNumber) {
        return toResponse(shipmentRepository.findByAwbNumber(awbNumber)
                .orElseThrow(() -> new IllegalArgumentException("Network shipment not found for AWB: " + awbNumber)));
    }

    private NetworkShipmentResponse toResponse(NetworkShipmentEntity shipment) {
        return mapper.toResponse(shipment, scanRepository.findByShipmentIdOrderByOccurredAtAsc(shipment.getShipmentId()));
    }
}
