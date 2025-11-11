package com.automatch.payments.infrastructure.sunat;

import java.math.BigDecimal;

public interface SunatInvoiceClient {
    void issueElectronicReceipt(String orderId, String currency, BigDecimal amount, String pspChargeId);
}