package com.iwos.darkstore.service;

import com.iwos.common.exception.ResourceNotFoundException;
import com.iwos.darkstore.entity.DarkStore;
import com.iwos.darkstore.repository.DarkStoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service @RequiredArgsConstructor @Slf4j @Transactional
public class DarkStoreService {

    private final DarkStoreRepository darkStoreRepository;

    public DarkStore createStore(DarkStore store) {
        DarkStore saved = darkStoreRepository.save(store);
        log.info("Dark store created: {} at ({}, {})", saved.getStoreCode(), saved.getLatitude(), saved.getLongitude());
        return saved;
    }

    @Transactional(readOnly = true)
    public DarkStore getStore(String id) {
        return darkStoreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DarkStore", "id", id));
    }

    @Transactional(readOnly = true)
    public List<DarkStore> findNearbyStores(double lat, double lng) {
        return darkStoreRepository.findNearbyStores(lat, lng);
    }

    @Transactional(readOnly = true)
    public List<DarkStore> getAll() {
        return darkStoreRepository.findAll();
    }
}
