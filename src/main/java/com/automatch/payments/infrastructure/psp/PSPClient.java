package com.automatch.payments.infrastructure.psp;

import java.math.BigDecimal;

public interface PSPClient {
    record ChargeResult(boolean success, String chargeId, String errorMessage) {
    }

    ChargeResult charge(String paymentMethodToken, String currency, BigDecimal amount, String orderId,
            String idempotencyKey);

    ChargeResult refund(String chargeId, String currency, BigDecimal amount, String idempotencyKey);
}