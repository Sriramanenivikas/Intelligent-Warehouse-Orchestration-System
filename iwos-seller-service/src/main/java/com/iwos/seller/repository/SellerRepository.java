package com.iwos.seller.repository;

import com.iwos.seller.entity.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SellerRepository extends JpaRepository<Seller, String> {
    Optional<Seller> findByUserId(String userId);
    Optional<Seller> findByGstin(String gstin);
    boolean existsByGstin(String gstin);
}
