package com.automatch.payments.domain.events;

import java.time.OffsetDateTime;

public record FraudAlertEvent(String requestId, String orderId, String reason,
        String currency, String amount, OffsetDateTime at) {
}