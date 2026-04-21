package com.iwos.shipmentnetwork.infrastructure.persistence;

import com.iwos.shipmentnetwork.api.http.NetworkShipmentResponse;
import com.iwos.shipmentnetwork.api.http.ScanEventResponse;
import com.iwos.shipmentnetwork.infrastructure.persistence.entity.NetworkShipmentEntity;
import com.iwos.shipmentnetwork.infrastructure.persistence.entity.ScanEventEntity;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class NetworkShipmentResponseMapper {

    public NetworkShipmentResponse toResponse(NetworkShipmentEntity shipment, List<ScanEventEntity> scans) {
        return new NetworkShipmentResponse(
                shipment.getNetworkShipmentId(),
                shipment.getShipmentId(),
                shipment.getFulfillmentOrderId(),
                shipment.getOrderIntentId(),
                shipment.getAwbNumber(),
                shipment.getCarrierCode(),
                shipment.getCustomerId(),
                shipment.getOriginNodeId(),
                shipment.getCurrentNodeId(),
                shipment.getCurrentFacilityCode(),
                shipment.getStatus().name(),
                shipment.getLastScanType() != null ? shipment.getLastScanType().name() : null,
                shipment.getLastScannedAt(),
                shipment.getCreatedAt(),
                shipment.getUpdatedAt(),
                scans.stream().map(this::toScanResponse).toList()
        );
    }

    public ScanEventResponse toScanResponse(ScanEventEntity scan) {
        return new ScanEventResponse(
                scan.getScanEventId(),
                scan.getShipmentId(),
                scan.getAwbNumber(),
                scan.getScanType().name(),
                scan.getNodeId(),
                scan.getFacilityCode(),
                scan.getNotes(),
                scan.getOccurredAt()
        );
    }
}
