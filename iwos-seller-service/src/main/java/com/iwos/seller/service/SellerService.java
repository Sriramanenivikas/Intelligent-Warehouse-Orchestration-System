package com.iwos.seller.service;

import com.iwos.common.exception.BusinessRuleException;
import com.iwos.common.exception.ResourceNotFoundException;
import com.iwos.seller.entity.Seller;
import com.iwos.seller.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor @Slf4j @Transactional
public class SellerService {

    private final SellerRepository sellerRepository;

    public Seller registerSeller(Seller seller) {
        if (sellerRepository.existsByGstin(seller.getGstin())) {
            throw new BusinessRuleException("Seller already registered with GSTIN: " + seller.getGstin());
        }
        Seller saved = sellerRepository.save(seller);
        log.info("Seller registered: {} (GSTIN: {})", saved.getBusinessName(), saved.getGstin());
        return saved;
    }

    @Transactional(readOnly = true)
    public Seller getSellerByUserId(String userId) {
        return sellerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller", "userId", userId));
    }

    public Seller approveSeller(String sellerId) {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller", "id", sellerId));
        seller.setStatus(Seller.SellerStatus.ACTIVE);
        return sellerRepository.save(seller);
    }
}
