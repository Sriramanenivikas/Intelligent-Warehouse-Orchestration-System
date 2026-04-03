package com.iwos.payment.infrastructure.observability;

import com.iwos.payment.infrastructure.persistence.repository.PaymentIntentRepository;
import com.iwos.payment.infrastructure.persistence.repository.PaymentOutboxEventRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.stereotype.Component;

@Component
public class PaymentMeterBinder implements MeterBinder {

    private final PaymentIntentRepository paymentIntentRepository;
    private final PaymentOutboxEventRepository paymentOutboxEventRepository;

    public PaymentMeterBinder(
            PaymentIntentRepository paymentIntentRepository,
            PaymentOutboxEventRepository paymentOutboxEventRepository
    ) {
        this.paymentIntentRepository = paymentIntentRepository;
        this.paymentOutboxEventRepository = paymentOutboxEventRepository;
    }

    @Override
    public void bindTo(MeterRegistry meterRegistry) {
        Gauge.builder("iwos.payment.intents.total", paymentIntentRepository, repository -> repository.count())
                .description("Total payment intents")
                .register(meterRegistry);
        Gauge.builder("iwos.payment.intents.authorized", paymentIntentRepository, repository -> repository.countByStatus("AUTHORIZED"))
                .description("Authorized payment intents")
                .register(meterRegistry);
        Gauge.builder("iwos.payment.outbox.pending", paymentOutboxEventRepository, repository -> repository.countByStatus("PENDING"))
                .description("Pending payment outbox events")
                .register(meterRegistry);
    }
}
