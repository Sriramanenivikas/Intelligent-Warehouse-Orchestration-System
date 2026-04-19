package com.iwos.scanevent.application;

import com.iwos.scanevent.api.http.ScanTimelineResponse;
import com.iwos.scanevent.domain.scan.TrackedShipmentNotFoundException;
import com.iwos.scanevent.infrastructure.persistence.ScanTimelineResponseMapper;
import com.iwos.scanevent.infrastructure.persistence.entity.TrackedShipmentEntity;
import com.iwos.scanevent.infrastructure.persistence.repository.NormalizedScanEventRepository;
import com.iwos.scanevent.infrastructure.persistence.repository.TrackedShipmentRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScanEventQueryService {

    private final TrackedShipmentRepository trackedShipmentRepository;
    private final NormalizedScanEventRepository normalizedScanEventRepository;
    private final ScanTimelineResponseMapper mapper;

    public ScanEventQueryService(
            TrackedShipmentRepository trackedShipmentRepository,
            NormalizedScanEventRepository normalizedScanEventRepository,
            ScanTimelineResponseMapper mapper
    ) {
        this.trackedShipmentRepository = trackedShipmentRepository;
        this.normalizedScanEventRepository = normalizedScanEventRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public ScanTimelineResponse getByShipmentId(UUID shipmentId) {
        TrackedShipmentEntity shipment = trackedShipmentRepository.findByShipmentId(shipmentId)
                .orElseThrow(() -> new TrackedShipmentNotFoundException(shipmentId));
        return mapper.toResponse(shipment, normalizedScanEventRepository.findByShipmentIdOrderByOccurredAtAsc(shipmentId));
    }

    @Transactional(readOnly = true)
    public ScanTimelineResponse getByOrderIntentId(UUID orderIntentId) {
        TrackedShipmentEntity shipment = trackedShipmentRepository.findByOrderIntentId(orderIntentId)
                .orElseThrow(() -> new TrackedShipmentNotFoundException("orderIntentId", orderIntentId.toString()));
        return mapper.toResponse(shipment, normalizedScanEventRepository.findByShipmentIdOrderByOccurredAtAsc(shipment.getShipmentId()));
    }

    @Transactional(readOnly = true)
    public ScanTimelineResponse getByAwbNumber(String awbNumber) {
        TrackedShipmentEntity shipment = trackedShipmentRepository.findByAwbNumberIgnoreCase(awbNumber)
                .orElseThrow(() -> new TrackedShipmentNotFoundException("awbNumber", awbNumber));
        return mapper.toResponse(shipment, normalizedScanEventRepository.findByShipmentIdOrderByOccurredAtAsc(shipment.getShipmentId()));
    }
}
