package com.iwos.serviceability.service;

import com.iwos.serviceability.entity.DeliveryPromise;
import com.iwos.serviceability.entity.PincodeMapping;
import com.iwos.serviceability.entity.ServiceZone;
import com.iwos.serviceability.repository.PincodeMappingRepository;
import com.iwos.serviceability.repository.ServiceZoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service @RequiredArgsConstructor @Slf4j @Transactional(readOnly = true)
public class ServiceabilityService {

    private final ServiceZoneRepository zoneRepository;
    private final PincodeMappingRepository pincodeRepository;
    private final EtaService etaService;

    public DeliveryPromise checkServiceability(double lat, double lng) {
        List<ServiceZone> zones = zoneRepository.findZonesForLocation(lat, lng);
        if (zones.isEmpty()) {
            return DeliveryPromise.builder()
                    .serviceable(false).displayText("Delivery not available in your area").build();
        }

        ServiceZone bestZone = zones.get(0);  // Sorted by delivery type (fastest first)
        int etaMinutes = etaService.calculateEta(bestZone, lat, lng);

        return DeliveryPromise.builder()
                .serviceable(true)
                .deliveryType(bestZone.getDeliveryType().name())
                .estimatedMinutes(etaMinutes)
                .displayText(etaService.formatEta(bestZone.getDeliveryType(), etaMinutes))
                .darkStoreId(bestZone.getDarkStoreId())
                .warehouseId(bestZone.getWarehouseId())
                .deliveryFee(etaMinutes <= 30 ? 25.0 : 0.0)
                .freeDeliveryAbove(199.0)
                .build();
    }

    public DeliveryPromise checkServiceabilityByPincode(String pincode) {
        PincodeMapping mapping = pincodeRepository.findByPincode(pincode).orElse(null);
        if (mapping == null || !mapping.isServiceable()) {
            return DeliveryPromise.builder()
                    .serviceable(false).displayText("Delivery not available for pincode: " + pincode).build();
        }

        return DeliveryPromise.builder()
                .serviceable(true)
                .deliveryType("STANDARD")
                .estimatedMinutes(mapping.getEstimatedHours() * 60)
                .displayText("Delivery in " + mapping.getEstimatedHours() + " hours")
                .warehouseId(mapping.getWarehouseId())
                .darkStoreId(mapping.getDarkStoreId())
                .build();
    }
}
