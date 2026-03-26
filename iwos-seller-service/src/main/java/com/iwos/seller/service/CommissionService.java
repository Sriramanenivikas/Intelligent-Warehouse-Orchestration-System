package com.iwos.seller.service;

import com.iwos.seller.entity.Commission;
import com.iwos.seller.entity.Seller;
import com.iwos.seller.repository.CommissionRepository;
import com.iwos.seller.repository.SellerRepository;
import com.iwos.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Service @RequiredArgsConstructor @Transactional
public class CommissionService {

    private final CommissionRepository commissionRepository;
    private final SellerRepository sellerRepository;

    public Commission calculateCommission(String sellerId, String orderId, BigDecimal orderAmount) {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller", "id", sellerId));

        BigDecimal rate = BigDecimal.valueOf(seller.getCommissionRate());
        BigDecimal commissionAmount = orderAmount.multiply(rate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal netPayable = orderAmount.subtract(commissionAmount);

        Commission commission = Commission.builder()
                .sellerId(sellerId).orderId(orderId).orderAmount(orderAmount)
                .commissionRate(rate).commissionAmount(commissionAmount)
                .netPayable(netPayable).createdAt(Instant.now()).build();
        return commissionRepository.save(commission);
    }
}
