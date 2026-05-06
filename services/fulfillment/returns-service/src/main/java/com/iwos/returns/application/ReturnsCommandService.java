package com.iwos.returns.application;

import com.iwos.returns.api.http.CreateReturnRequest;
import com.iwos.returns.api.http.ReturnResponse;
import com.iwos.returns.domain.ReturnRequestNotFoundException;
import com.iwos.returns.domain.ReturnRequestStatusException;
import com.iwos.returns.infrastructure.persistence.ReturnResponseMapper;
import com.iwos.returns.infrastructure.persistence.entity.ReturnRequestEntity;
import com.iwos.returns.infrastructure.persistence.repository.ReturnRequestRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReturnsCommandService {

    private final ReturnRequestRepository returnRequestRepository;
    private final ReturnResponseMapper returnResponseMapper;

    public ReturnsCommandService(
            ReturnRequestRepository returnRequestRepository,
            ReturnResponseMapper returnResponseMapper
    ) {
        this.returnRequestRepository = returnRequestRepository;
        this.returnResponseMapper = returnResponseMapper;
    }

    public ReturnResponse createReturnRequest(CreateReturnRequest request) {
        Instant now = Instant.now();
        ReturnRequestEntity entity = new ReturnRequestEntity();
        entity.setReturnRequestId(UUID.randomUUID());
        entity.setOrderIntentId(request.orderIntentId());
        entity.setFulfillmentOrderId(request.fulfillmentOrderId());
        entity.setShipmentId(request.shipmentId());
        entity.setCustomerId(request.customerId().trim());
        entity.setNodeId(request.nodeId().trim());
        entity.setReasonCode(request.reasonCode().trim().toUpperCase());
        entity.setReasonDetail(request.reasonDetail());
        entity.setStatus("REQUESTED");
        entity.setItemCount(request.items().size());
        entity.setItemsJson(returnResponseMapper.writeItems(request.items()));
        entity.setRequestedAt(now);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        return returnResponseMapper.toResponse(returnRequestRepository.save(entity));
    }

    public ReturnResponse approveReturnRequest(UUID returnRequestId) {
        ReturnRequestEntity entity = findExisting(returnRequestId);
        if (!"REQUESTED".equals(entity.getStatus())) {
            throw new ReturnRequestStatusException("Only REQUESTED return requests can be approved");
        }
        Instant now = Instant.now();
        entity.setStatus("APPROVED");
        entity.setApprovedAt(now);
        entity.setUpdatedAt(now);
        return returnResponseMapper.toResponse(returnRequestRepository.save(entity));
    }

    public ReturnResponse markReceived(UUID returnRequestId) {
        ReturnRequestEntity entity = findExisting(returnRequestId);
        if (!"APPROVED".equals(entity.getStatus())) {
            throw new ReturnRequestStatusException("Only APPROVED return requests can be marked as received");
        }
        Instant now = Instant.now();
        entity.setStatus("RECEIVED");
        entity.setReceivedAt(now);
        entity.setUpdatedAt(now);
        return returnResponseMapper.toResponse(returnRequestRepository.save(entity));
    }

    private ReturnRequestEntity findExisting(UUID returnRequestId) {
        return returnRequestRepository.findById(returnRequestId)
                .orElseThrow(() -> new ReturnRequestNotFoundException(returnRequestId));
    }
}
