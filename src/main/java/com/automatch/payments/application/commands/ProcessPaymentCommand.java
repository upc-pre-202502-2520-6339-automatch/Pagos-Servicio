package com.automatch.payments.application.commands;

import java.math.BigDecimal;

public record ProcessPaymentCommand(

                String requestId, // idempotencia
                String orderId,
                String buyerId,
                String ipAddress,
                String cardFingerprint, // o hash del método
                String currency,
                BigDecimal amount,
                String paymentMethodToken // token PSP (card/bank/wallet)
) {
}