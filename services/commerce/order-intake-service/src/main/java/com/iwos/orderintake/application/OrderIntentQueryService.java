package com.iwos.orderintake.application;

import com.iwos.orderintake.api.http.OrderIntentResponse;
import com.iwos.orderintake.domain.order.OrderIntentNotFoundException;
import com.iwos.orderintake.infrastructure.persistence.OrderIntentResponseMapper;
import com.iwos.orderintake.infrastructure.persistence.repository.OrderIntentRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Provides query access to accepted order intents.
 */
@Service
public class OrderIntentQueryService {

    private final OrderIntentRepository orderIntentRepository;
    private final OrderIntentResponseMapper responseMapper;

    public OrderIntentQueryService(
            OrderIntentRepository orderIntentRepository,
            OrderIntentResponseMapper responseMapper
    ) {
        this.orderIntentRepository = orderIntentRepository;
        this.responseMapper = responseMapper;
    }

    @Transactional(readOnly = true)
    public OrderIntentResponse get(UUID orderIntentId) {
        return orderIntentRepository.findByOrderIntentId(orderIntentId)
                .map(responseMapper::toResponse)
                .orElseThrow(() -> new OrderIntentNotFoundException(orderIntentId));
    }
}
