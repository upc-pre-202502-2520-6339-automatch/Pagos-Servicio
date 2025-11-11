package com.automatch.payments.infrastructure.antifraud;

import com.automatch.payments.domain.model.FraudCheckResult;
import com.automatch.payments.domain.model.Money;
import com.automatch.payments.domain.services.AntiFraudService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SimpleAntiFraudService implements AntiFraudService {

    private static final BigDecimal MAX_SINGLE_TICKET = new BigDecimal("50000.00");
    private static final Set<String> blacklistedFingerprints = ConcurrentHashMap.newKeySet();

    @Override
    public FraudCheckResult preAuthorize(String buyerId, String cardFingerprint, Money money, String ipAddress) {
        if (money.amount().compareTo(MAX_SINGLE_TICKET) > 0)
            return FraudCheckResult.suspicious("Amount exceeds limit");

        if (cardFingerprint != null && blacklistedFingerprints.contains(cardFingerprint))
            return FraudCheckResult.suspicious("Card fingerprint blacklisted");

        // ejemplo: bloquear IPs de riesgo (solo demo)
        if (ipAddress != null && ipAddress.startsWith("10.")) // imaginemos que 10.x es sospechoso
            return FraudCheckResult.suspicious("Risky IP segment");

        return FraudCheckResult.ok();
    }
}