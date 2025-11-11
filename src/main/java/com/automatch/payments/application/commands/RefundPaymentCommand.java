package com.automatch.payments.application.commands;

import java.math.BigDecimal;

public record RefundPaymentCommand(
        String requestId,
        String orderId,
        String pspChargeId,
        BigDecimal amount,
        String currency) {
}