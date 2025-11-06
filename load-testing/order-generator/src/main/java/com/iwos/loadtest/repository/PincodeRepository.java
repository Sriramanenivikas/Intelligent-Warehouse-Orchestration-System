package com.iwos.loadtest.repository;

import com.iwos.loadtest.model.Pincode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PincodeRepository extends JpaRepository<Pincode, String> {

    /**
     * Get random pincode for order generation
     */
    @Query(value = "SELECT * FROM pincodes ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    Pincode findRandomPincode();

    /**
     * Get all pincodes from a specific city
     */
    List<Pincode> findByCity(String city);

    /**
     * Count pincodes by city
     */
    long countByCity(String city);
}
