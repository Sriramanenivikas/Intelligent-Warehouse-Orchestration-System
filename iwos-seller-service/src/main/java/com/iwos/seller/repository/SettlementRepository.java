package com.iwos.seller.repository;

import com.iwos.seller.entity.Settlement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, String> {
    Page<Settlement> findBySellerIdOrderByCreatedAtDesc(String sellerId, Pageable pageable);
}
