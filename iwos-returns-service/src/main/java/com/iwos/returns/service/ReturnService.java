package com.iwos.returns.service;

import com.iwos.common.exception.BusinessRuleException;
import com.iwos.common.exception.ResourceNotFoundException;
import com.iwos.returns.entity.ReturnRequest;
import com.iwos.returns.repository.ReturnRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor @Slf4j @Transactional
public class ReturnService {

    private final ReturnRequestRepository returnRepository;

    public ReturnRequest createReturnRequest(ReturnRequest request) {
        long existingReturns = returnRepository.countByOrderIdAndStatusNot(
                request.getOrderId(), ReturnRequest.ReturnStatus.REJECTED);
        if (existingReturns > 0) {
            throw new BusinessRuleException("Return already exists for order: " + request.getOrderId());
        }
        ReturnRequest saved = returnRepository.save(request);
        log.info("Return request created: {} for order: {}", saved.getId(), saved.getOrderId());
        return saved;
    }

    public ReturnRequest approveReturn(String returnId) {
        ReturnRequest req = findById(returnId);
        req.setStatus(ReturnRequest.ReturnStatus.APPROVED);
        return returnRepository.save(req);
    }

    public ReturnRequest schedulePickup(String returnId, String pickupAddress) {
        ReturnRequest req = findById(returnId);
        req.setStatus(ReturnRequest.ReturnStatus.PICKUP_SCHEDULED);
        req.setPickupAddress(pickupAddress);
        return returnRepository.save(req);
    }

    public ReturnRequest updateStatus(String returnId, ReturnRequest.ReturnStatus status) {
        ReturnRequest req = findById(returnId);
        req.setStatus(status);
        return returnRepository.save(req);
    }

    @Transactional(readOnly = true)
    public Page<ReturnRequest> getReturnsByUser(String userId, Pageable pageable) {
        return returnRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    private ReturnRequest findById(String id) {
        return returnRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ReturnRequest", "id", id));
    }
}
