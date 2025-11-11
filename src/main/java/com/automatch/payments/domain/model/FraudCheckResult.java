package com.automatch.payments.domain.model;

public record FraudCheckResult(boolean suspicious, String reason) {
    public static FraudCheckResult ok() {
        return new FraudCheckResult(false, null);
    }

    public static FraudCheckResult suspicious(String reason) {
        return new FraudCheckResult(true, reason);
    }
}