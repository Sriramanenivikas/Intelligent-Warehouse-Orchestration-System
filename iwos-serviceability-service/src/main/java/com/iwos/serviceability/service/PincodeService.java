package com.iwos.serviceability.service;

import com.iwos.serviceability.entity.PincodeMapping;
import com.iwos.serviceability.repository.PincodeMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor @Transactional
public class PincodeService {
    private final PincodeMappingRepository repository;

    public PincodeMapping addPincode(PincodeMapping mapping) {
        return repository.save(mapping);
    }

    @Transactional(readOnly = true)
    public PincodeMapping getByPincode(String pincode) {
        return repository.findByPincode(pincode).orElse(null);
    }
}
