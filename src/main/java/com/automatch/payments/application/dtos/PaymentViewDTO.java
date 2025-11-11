package com.automatch.payments.application.dtos;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PaymentViewDTO(
        String requestId,
        String orderId,
        String status,
        String pspChargeId,
        String currency,
        BigDecimal amount,
        OffsetDateTime createdAt,
        String reason) {
}