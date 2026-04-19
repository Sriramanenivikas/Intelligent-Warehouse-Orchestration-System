package com.iwos.scanevent.infrastructure.persistence;

import com.iwos.scanevent.api.http.NormalizedScanEventResponse;
import com.iwos.scanevent.api.http.ScanTimelineResponse;
import com.iwos.scanevent.infrastructure.persistence.entity.NormalizedScanEventEntity;
import com.iwos.scanevent.infrastructure.persistence.entity.TrackedShipmentEntity;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ScanTimelineResponseMapper {

    public ScanTimelineResponse toResponse(
            TrackedShipmentEntity trackedShipment,
            List<NormalizedScanEventEntity> events
    ) {
        return new ScanTimelineResponse(
                trackedShipment.getTrackedShipmentId(),
                trackedShipment.getShipmentId(),
                trackedShipment.getNetworkShipmentId(),
                trackedShipment.getFulfillmentOrderId(),
                trackedShipment.getOrderIntentId(),
                trackedShipment.getAwbNumber(),
                trackedShipment.getCarrierCode(),
                trackedShipment.getCustomerId(),
                trackedShipment.getCurrentStatus().name(),
                trackedShipment.getLastScanType().name(),
                trackedShipment.getLastScannedAt(),
                trackedShipment.getCreatedAt(),
                trackedShipment.getUpdatedAt(),
                events.stream().map(this::toEventResponse).toList()
        );
    }

    private NormalizedScanEventResponse toEventResponse(NormalizedScanEventEntity event) {
        return new NormalizedScanEventResponse(
                event.getNormalizedScanEventId(),
                event.getScanEventId(),
                event.getSourceEventType(),
                event.getScanType().name(),
                event.getStatusAfterEvent().name(),
                event.getNodeId(),
                event.getFacilityCode(),
                event.getNotes(),
                event.getOccurredAt(),
                event.getIngestedAt()
        );
    }
}
