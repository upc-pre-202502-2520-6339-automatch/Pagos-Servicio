package com.automatch.payments.infrastructure.sunat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class SunatInvoiceClientImpl implements SunatInvoiceClient {

    private final String baseUrl;
    private final String clientId;
    private final String clientSecret;
    private final String emitterRuc;

    public SunatInvoiceClientImpl(
            @Value("${sunat.baseUrl}") String baseUrl,
            @Value("${sunat.clientId}") String clientId,
            @Value("${sunat.clientSecret}") String clientSecret,
            @Value("${sunat.emitter.ruc}") String emitterRuc) {
        this.baseUrl = baseUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.emitterRuc = emitterRuc;
    }

    @Override
    public void issueElectronicReceipt(String orderId, String currency, BigDecimal amount, String pspChargeId) {
    }
}