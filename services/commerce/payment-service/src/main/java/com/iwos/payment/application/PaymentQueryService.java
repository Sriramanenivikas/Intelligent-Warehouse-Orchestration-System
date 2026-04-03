package com.iwos.payment.application;

import com.iwos.payment.api.http.PaymentIntentResponse;
import com.iwos.payment.domain.payment.PaymentIntentNotFoundException;
import com.iwos.payment.infrastructure.persistence.PaymentIntentResponseMapper;
import com.iwos.payment.infrastructure.persistence.repository.PaymentIntentRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class PaymentQueryService {

    private final PaymentIntentRepository paymentIntentRepository;
    private final PaymentIntentResponseMapper responseMapper;

    public PaymentQueryService(
            PaymentIntentRepository paymentIntentRepository,
            PaymentIntentResponseMapper responseMapper
    ) {
        this.paymentIntentRepository = paymentIntentRepository;
        this.responseMapper = responseMapper;
    }

    public PaymentIntentResponse findById(UUID paymentIntentId) {
        return paymentIntentRepository.findByPaymentIntentId(paymentIntentId)
                .map(responseMapper::toResponse)
                .orElseThrow(() -> new PaymentIntentNotFoundException(paymentIntentId));
    }
}
