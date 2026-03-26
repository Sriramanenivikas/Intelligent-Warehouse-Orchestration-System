package com.iwos.seller.service;

import com.iwos.seller.entity.Settlement;
import com.iwos.seller.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor @Transactional
public class SettlementService {

    private final SettlementRepository settlementRepository;

    @Transactional(readOnly = true)
    public Page<Settlement> getSettlements(String sellerId, Pageable pageable) {
        return settlementRepository.findBySellerIdOrderByCreatedAtDesc(sellerId, pageable);
    }
}
