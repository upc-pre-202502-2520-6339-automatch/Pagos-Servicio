package com.automatch.payments.infrastructure.outbox;

import com.automatch.payments.domain.events.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OutboxPublisher {
    private final OutboxEventRepository repo;
    private final KafkaTemplate<String, Object> kafka;
    private final ObjectMapper mapper;

    public OutboxPublisher(OutboxEventRepository repo, KafkaTemplate<String, Object> kafka, ObjectMapper mapper) {
        this.repo = repo;
        this.kafka = kafka;
        this.mapper = mapper;
    }

    @Scheduled(fixedDelay = 1000)
    public void publishPending() {
        List<OutboxEvent> batch = repo.findTop50ByPublishedFalseOrderByCreatedAtAsc();
        for (OutboxEvent e : batch) {
            try {
                Object payload = switch (e.getType()) {
                    case "PaymentConfirmedEvent" -> mapper.readValue(e.getPayload(), PaymentConfirmedEvent.class);
                    case "RefundedEvent" -> mapper.readValue(e.getPayload(), RefundedEvent.class);
                    case "FraudAlertEvent" -> mapper.readValue(e.getPayload(), FraudAlertEvent.class);
                    default -> throw new IllegalArgumentException("Unknown type " + e.getType());
                };
                // ruteo por tipo de evento
                String topic = switch (e.getType()) {
                    case "PaymentConfirmedEvent" -> "payments.confirmed";
                    case "RefundedEvent" -> "payments.refunded";
                    case "FraudAlertEvent" -> "payments.fraud";
                    default -> throw new IllegalStateException();
                };
                kafka.send(topic, e.getAggregateId(), payload).get(); // envío síncrono
                e.setPublished(true);
                repo.save(e);
            } catch (Exception ex) {
            }
        }
    }
}