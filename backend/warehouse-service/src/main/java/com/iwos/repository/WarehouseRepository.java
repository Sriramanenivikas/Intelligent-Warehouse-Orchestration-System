package com.iwos.repository;

import com.iwos.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Warehouse Repository with PostGIS geospatial queries
 */
@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    /**
     * Find warehouse by code
     */
    Optional<Warehouse> findByCode(String code);

    /**
     * Find all active warehouses
     */
    List<Warehouse> findByIsActiveTrue();

    /**
     * Find warehouses within a radius using Haversine formula
     * This is a pure Java implementation without PostGIS dependency
     *
     * @param latitude Customer latitude
     * @param longitude Customer longitude
     * @param radiusKm Search radius in kilometers
     * @return List of warehouses within the radius
     */
    @Query("""
        SELECT w FROM Warehouse w
        WHERE w.isActive = true
        AND (
            6371 * acos(
                cos(radians(:latitude)) *
                cos(radians(w.latitude)) *
                cos(radians(w.longitude) - radians(:longitude)) +
                sin(radians(:latitude)) *
                sin(radians(w.latitude))
            )
        ) <= :radiusKm
        ORDER BY (
            6371 * acos(
                cos(radians(:latitude)) *
                cos(radians(w.latitude)) *
                cos(radians(w.longitude) - radians(:longitude)) +
                sin(radians(:latitude)) *
                sin(radians(w.latitude))
            )
        )
        """)
    List<Warehouse> findWarehousesWithinRadius(
        @Param("latitude") Double latitude,
        @Param("longitude") Double longitude,
        @Param("radiusKm") Double radiusKm
    );

    /**
     * Find warehouses with available capacity
     */
    @Query("SELECT w FROM Warehouse w WHERE w.isActive = true AND w.currentLoad < w.maxCapacity")
    List<Warehouse> findWarehousesWithAvailableCapacity();

    /**
     * Find warehouses by city
     */
    List<Warehouse> findByCityIgnoreCase(String city);

    /**
     * Find warehouses by state
     */
    List<Warehouse> findByStateIgnoreCase(String state);

    /**
     * Find warehouses by country
     */
    List<Warehouse> findByCountryIgnoreCase(String country);

    /**
     * Find warehouses with priority greater than or equal to specified value
     */
    List<Warehouse> findByPriorityGreaterThanEqualAndIsActiveTrue(Integer priority);

    /**
     * Count active warehouses
     */
    @Query("SELECT COUNT(w) FROM Warehouse w WHERE w.isActive = true")
    Long countActiveWarehouses();

    /**
     * Find warehouses with load percentage below threshold
     */
    @Query("SELECT w FROM Warehouse w WHERE w.isActive = true AND (CAST(w.currentLoad AS double) / w.maxCapacity) < :threshold")
    List<Warehouse> findWarehousesWithLoadBelowThreshold(@Param("threshold") Double threshold);
}

