package com.automatch.payments.domain.services;

import com.automatch.payments.domain.model.FraudCheckResult;
import com.automatch.payments.domain.model.Money;

public interface AntiFraudService {
    FraudCheckResult preAuthorize(String buyerId, String cardFingerprint, Money money, String ipAddress);
}