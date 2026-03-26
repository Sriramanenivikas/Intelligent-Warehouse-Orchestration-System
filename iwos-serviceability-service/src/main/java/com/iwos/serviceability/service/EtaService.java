package com.iwos.serviceability.service;

import com.iwos.serviceability.entity.ServiceZone;
import org.springframework.stereotype.Service;

@Service
public class EtaService {

    public int calculateEta(ServiceZone zone, double customerLat, double customerLng) {
        double distance = haversineKm(zone.getCenterLat(), zone.getCenterLng(), customerLat, customerLng);
        return switch (zone.getDeliveryType()) {
            case EXPRESS_10MIN -> (int) Math.max(8, 10 + distance * 2);
            case EXPRESS_30MIN -> (int) Math.max(20, 30 + distance * 3);
            case SAME_DAY -> 240;  // 4 hours
            case NEXT_DAY -> 1440; // 24 hours
            case STANDARD -> 2880; // 48 hours
        };
    }

    public String formatEta(ServiceZone.DeliveryType type, int minutes) {
        return switch (type) {
            case EXPRESS_10MIN -> "Delivery in " + minutes + " minutes 🚀";
            case EXPRESS_30MIN -> "Delivery in " + minutes + " minutes";
            case SAME_DAY -> "Same day delivery";
            case NEXT_DAY -> "Delivery by tomorrow";
            case STANDARD -> "Delivery in 2-3 days";
        };
    }

    private double haversineKm(double lat1, double lng1, double lat2, double lng2) {
        double R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
