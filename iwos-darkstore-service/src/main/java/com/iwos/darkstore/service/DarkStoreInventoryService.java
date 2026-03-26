package com.iwos.darkstore.service;

import com.iwos.common.exception.BusinessRuleException;
import com.iwos.common.exception.ResourceNotFoundException;
import com.iwos.darkstore.entity.DarkStoreStock;
import com.iwos.darkstore.repository.DarkStoreStockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service @RequiredArgsConstructor @Slf4j @Transactional
public class DarkStoreInventoryService {

    private final DarkStoreStockRepository stockRepository;
    private final ReplenishmentService replenishmentService;

    public DarkStoreStock addStock(String storeId, String skuCode, String productId, int quantity) {
        DarkStoreStock stock = stockRepository.findByStoreIdAndSkuCode(storeId, skuCode)
                .orElse(DarkStoreStock.builder().storeId(storeId).skuCode(skuCode).productId(productId).build());
        stock.setQuantity(stock.getQuantity() + quantity);
        DarkStoreStock saved = stockRepository.save(stock);
        log.info("Stock added: {} units of {} at store {}", quantity, skuCode, storeId);
        return saved;
    }

    public boolean reserveStock(String storeId, String skuCode, int quantity) {
        DarkStoreStock stock = stockRepository.findByStoreIdAndSkuCode(storeId, skuCode)
                .orElseThrow(() -> new ResourceNotFoundException("Stock", "skuCode", skuCode));
        if (stock.getAvailableQuantity() < quantity) {
            throw new BusinessRuleException("Insufficient stock at dark store for SKU: " + skuCode);
        }
        stock.setReservedQuantity(stock.getReservedQuantity() + quantity);
        stockRepository.save(stock);

        if (stock.needsReplenishment()) {
            replenishmentService.triggerReplenishment(storeId, skuCode, stock.getMaxLevel() - stock.getQuantity());
        }
        return true;
    }

    public void confirmDeduction(String storeId, String skuCode, int quantity) {
        DarkStoreStock stock = stockRepository.findByStoreIdAndSkuCode(storeId, skuCode)
                .orElseThrow(() -> new ResourceNotFoundException("Stock", "skuCode", skuCode));
        stock.setQuantity(stock.getQuantity() - quantity);
        stock.setReservedQuantity(Math.max(0, stock.getReservedQuantity() - quantity));
        stockRepository.save(stock);
    }

    @Transactional(readOnly = true)
    public boolean checkAvailability(String storeId, String skuCode, int quantity) {
        return stockRepository.findByStoreIdAndSkuCode(storeId, skuCode)
                .map(s -> s.getAvailableQuantity() >= quantity).orElse(false);
    }

    @Transactional(readOnly = true)
    public List<DarkStoreStock> getLowStockItems(String storeId) {
        return stockRepository.findLowStockItems(storeId);
    }
}
