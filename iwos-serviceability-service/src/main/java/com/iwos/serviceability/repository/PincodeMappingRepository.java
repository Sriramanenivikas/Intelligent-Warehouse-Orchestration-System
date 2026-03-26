package com.iwos.serviceability.repository;

import com.iwos.serviceability.entity.PincodeMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PincodeMappingRepository extends JpaRepository<PincodeMapping, String> {
    Optional<PincodeMapping> findByPincode(String pincode);
}
