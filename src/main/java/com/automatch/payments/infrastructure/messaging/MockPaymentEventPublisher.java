package com.automatch.payments.infrastructure.messaging;

import com.automatch.payments.domain.events.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Primary // ✅ ESTE es el que Spring usará por defecto
@ConditionalOnProperty(name = "payments.kafka.enabled", havingValue = "false", matchIfMissing = true)
public class MockPaymentEventPublisher implements PaymentEventPublisher {

    @Override
    public void publish(PaymentConfirmedEvent event) {
        log.info("✅ [MOCK] PaymentConfirmedEvent skipped Kafka publish: {}", event);
    }

    @Override
    public void publish(RefundedEvent event) {
        log.info("✅ [MOCK] RefundedEvent skipped Kafka publish: {}", event);
    }

    @Override
    public void publish(FraudAlertEvent event) {
        log.info("✅ [MOCK] FraudAlertEvent skipped Kafka publish: {}", event);
    }
}
