package com.iwos.darkstore.service;

import com.iwos.darkstore.entity.BatchPick;
import com.iwos.darkstore.repository.BatchPickRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service @RequiredArgsConstructor @Slf4j @Transactional
public class BatchPickingService {

    private final BatchPickRepository batchPickRepository;

    public BatchPick createBatchPick(String storeId, String pickerId, List<String> orderIds, int totalItems) {
        BatchPick pick = BatchPick.builder()
                .storeId(storeId).pickerId(pickerId)
                .orderIds(orderIds).totalItems(totalItems).build();
        BatchPick saved = batchPickRepository.save(pick);
        log.info("Batch pick created at store {} for {} orders", storeId, orderIds.size());
        return saved;
    }

    public BatchPick completeBatchPick(String batchId) {
        BatchPick pick = batchPickRepository.findById(batchId).orElseThrow();
        pick.setStatus(BatchPick.BatchPickStatus.COMPLETED);
        pick.setCompletedAt(Instant.now());
        return batchPickRepository.save(pick);
    }
}
