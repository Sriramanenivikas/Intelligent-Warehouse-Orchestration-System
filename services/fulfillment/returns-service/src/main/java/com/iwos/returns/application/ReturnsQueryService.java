package com.iwos.returns.application;

import com.iwos.returns.api.http.ReturnResponse;
import com.iwos.returns.domain.ReturnRequestNotFoundException;
import com.iwos.returns.infrastructure.persistence.ReturnResponseMapper;
import com.iwos.returns.infrastructure.persistence.entity.ReturnRequestEntity;
import com.iwos.returns.infrastructure.persistence.repository.ReturnRequestRepository;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReturnsQueryService {

    private final ReturnRequestRepository returnRequestRepository;
    private final ReturnResponseMapper returnResponseMapper;

    public ReturnsQueryService(
            ReturnRequestRepository returnRequestRepository,
            ReturnResponseMapper returnResponseMapper
    ) {
        this.returnRequestRepository = returnRequestRepository;
        this.returnResponseMapper = returnResponseMapper;
    }

    public List<ReturnResponse> listReturns(String status, String customerId) {
        if ((status == null || status.isBlank()) && (customerId == null || customerId.isBlank())) {
            return returnRequestRepository.findAllByOrderByRequestedAtDesc().stream()
                    .map(returnResponseMapper::toResponse)
                    .toList();
        }
        Specification<ReturnRequestEntity> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null && !status.isBlank()) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.upper(root.get("status")),
                        status.trim().toUpperCase(Locale.ROOT)
                ));
            }
            if (customerId != null && !customerId.isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("customerId"), customerId.trim()));
            }
            query.orderBy(criteriaBuilder.desc(root.get("requestedAt")));
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
        return returnRequestRepository.findAll(specification).stream()
                .map(returnResponseMapper::toResponse)
                .toList();
    }

    public ReturnResponse getReturnRequest(UUID returnRequestId) {
        return returnRequestRepository.findById(returnRequestId)
                .map(returnResponseMapper::toResponse)
                .orElseThrow(() -> new ReturnRequestNotFoundException(returnRequestId));
    }
}
