package com.automatch.payments.infrastructure.psp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class StripeClient implements PSPClient {

    private final String apiKey;

    public StripeClient(@Value("${psp.stripe.apiKey}") String apiKey) {
        this.apiKey = apiKey;
        // Stripe.apiKey = apiKey; // cuando agregues la dependencia
    }

    @Override
    public ChargeResult charge(String paymentMethodToken, String currency, BigDecimal amount, String orderId,
            String idempotencyKey) {
        try {

            // Demo sin SDK:
            return new ChargeResult(true, "ch_test_" + idempotencyKey, null);
        } catch (Exception e) {
            return new ChargeResult(false, null, e.getMessage());
        }
    }

    @Override
    public ChargeResult refund(String chargeId, String currency, BigDecimal amount, String idempotencyKey) {
        try {

            return new ChargeResult(true, chargeId, null);
        } catch (Exception e) {
            return new ChargeResult(false, null, e.getMessage());
        }
    }
}