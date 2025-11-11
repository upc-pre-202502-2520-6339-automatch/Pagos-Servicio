package com.automatch.payments.infrastructure.messaging;

import com.automatch.payments.domain.events.*;

public interface PaymentEventPublisher {
    void publish(PaymentConfirmedEvent event);

    void publish(RefundedEvent event);

    void publish(FraudAlertEvent event);

}