package com.iwos.darkstore.repository;

import com.iwos.darkstore.entity.DarkStore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DarkStoreRepository extends JpaRepository<DarkStore, String> {
    Optional<DarkStore> findByStoreCode(String storeCode);
    List<DarkStore> findByStatusAndCity(DarkStore.StoreStatus status, String city);

    @Query(value = "SELECT * FROM dark_stores WHERE status = 'ACTIVE' " +
           "AND (6371 * acos(cos(radians(:lat)) * cos(radians(latitude)) * " +
           "cos(radians(longitude) - radians(:lng)) + sin(radians(:lat)) * " +
           "sin(radians(latitude)))) <= service_radius_km ORDER BY " +
           "(6371 * acos(cos(radians(:lat)) * cos(radians(latitude)) * " +
           "cos(radians(longitude) - radians(:lng)) + sin(radians(:lat)) * " +
           "sin(radians(latitude)))) ASC", nativeQuery = true)
    List<DarkStore> findNearbyStores(@Param("lat") double lat, @Param("lng") double lng);
}
