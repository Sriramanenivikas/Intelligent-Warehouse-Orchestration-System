package com.iwos.darkstore.service;

import com.iwos.darkstore.entity.DarkStore;
import com.iwos.darkstore.entity.ReplenishmentOrder;
import com.iwos.darkstore.repository.DarkStoreRepository;
import com.iwos.darkstore.repository.ReplenishmentOrderRepository;
import com.iwos.darkstore.repository.DarkStoreStockRepository;
import com.iwos.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor @Slf4j @Transactional
public class ReplenishmentService {

    private final ReplenishmentOrderRepository replenishmentRepository;
    private final DarkStoreRepository storeRepository;
    private final DarkStoreStockRepository stockRepository;

    public void triggerReplenishment(String storeId, String skuCode, int quantity) {
        DarkStore store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("DarkStore", "id", storeId));

        ReplenishmentOrder order = ReplenishmentOrder.builder()
                .storeId(storeId)
                .skuCode(skuCode)
                .requestedQuantity(quantity)
                .sourceWarehouseId(store.getParentWarehouseId())
                .build();

        replenishmentRepository.save(order);
        log.info("Replenishment triggered: {} units of {} for store {}", quantity, skuCode, storeId);
    }

    @Scheduled(fixedRate = 300000)  // Every 5 minutes
    public void autoReplenishCheck() {
        storeRepository.findAll().stream()
                .filter(s -> s.getStatus() == DarkStore.StoreStatus.ACTIVE)
                .forEach(store -> {
                    stockRepository.findLowStockItems(store.getId()).forEach(stock -> {
                        int needed = stock.getMaxLevel() - stock.getQuantity();
                        if (needed > 0) {
                            triggerReplenishment(store.getId(), stock.getSkuCode(), needed);
                        }
                    });
                });
    }
}
