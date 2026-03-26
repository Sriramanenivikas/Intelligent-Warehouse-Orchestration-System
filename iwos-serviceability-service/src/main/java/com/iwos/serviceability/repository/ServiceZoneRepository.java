package com.iwos.serviceability.repository;

import com.iwos.serviceability.entity.ServiceZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ServiceZoneRepository extends JpaRepository<ServiceZone, String> {

    @Query(value = "SELECT * FROM service_zones WHERE active = true AND " +
           "(6371 * acos(cos(radians(:lat)) * cos(radians(center_lat)) * " +
           "cos(radians(center_lng) - radians(:lng)) + sin(radians(:lat)) * " +
           "sin(radians(center_lat)))) <= radius_km " +
           "ORDER BY delivery_type ASC", nativeQuery = true)
    List<ServiceZone> findZonesForLocation(@Param("lat") double lat, @Param("lng") double lng);
}
