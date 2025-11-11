package com.automatch.payments.infrastructure.messaging;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaProducer {

    private final KafkaTemplate<String, Object> template;

    public KafkaProducer(KafkaTemplate<String, Object> template) {
        this.template = template;
    }

    public void publish(String topic, String key, Object payload) {
        template.send(topic, key, payload);
    }
}