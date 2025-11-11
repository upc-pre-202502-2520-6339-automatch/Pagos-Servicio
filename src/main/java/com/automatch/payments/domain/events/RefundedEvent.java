package com.automatch.payments.domain.events;

import java.time.OffsetDateTime;

public record RefundedEvent(String requestId, String orderId, String pspChargeId,
        String currency, String amount, OffsetDateTime at) {
}
