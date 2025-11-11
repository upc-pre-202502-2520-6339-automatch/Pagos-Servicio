package com.automatch.payments.infrastructure.messaging;

import com.automatch.payments.domain.events.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaPaymentEventPublisher implements PaymentEventPublisher {

    private final KafkaTemplate<String, Object> kafka;
    private final String confirmedTopic;
    private final String refundedTopic;
    private final String fraudTopic;

    public KafkaPaymentEventPublisher(
            KafkaTemplate<String, Object> kafka,
            org.springframework.core.env.Environment env) {
        this.kafka = kafka;
        this.confirmedTopic = env.getProperty("payments.kafka.topic.confirmed");
        this.refundedTopic = env.getProperty("payments.kafka.topic.refunded");
        this.fraudTopic = env.getProperty("payments.kafka.topic.fraud");
    }

    @Override
    public void publish(PaymentConfirmedEvent event) {
        kafka.send(confirmedTopic, event.requestId(), event);
    }

    @Override
    public void publish(RefundedEvent event) {
        kafka.send(refundedTopic, event.requestId(), event);
    }

    @Override
    public void publish(FraudAlertEvent event) {
        kafka.send(fraudTopic, event.requestId(), event);
    }
}