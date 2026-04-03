package com.iwos.payment.infrastructure.persistence;

import com.iwos.payment.api.http.PaymentIntentResponse;
import com.iwos.payment.infrastructure.persistence.entity.PaymentIntentEntity;
import org.springframework.stereotype.Component;

@Component
public class PaymentIntentResponseMapper {

    public PaymentIntentResponse toResponse(PaymentIntentEntity entity) {
        return new PaymentIntentResponse(
                entity.getPaymentIntentId(),
                entity.getOrderIntentId(),
                entity.getOrderWorkflowId(),
                entity.getCustomerId(),
                entity.getPaymentMode(),
                entity.getCurrency(),
                entity.getTotalAmount(),
                entity.getCapturedAmount(),
                entity.getProviderName(),
                entity.getProviderReference(),
                entity.getStatus(),
                entity.getFailureReason(),
                entity.getAuthorizedAt(),
                entity.getSucceededAt(),
                entity.getFailedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
