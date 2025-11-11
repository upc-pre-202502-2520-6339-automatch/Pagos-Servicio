package com.automatch.payments.application.dtos;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record PaymentRequestDTO(
        @NotBlank String requestId,
        @NotBlank String orderId,
        @NotBlank String buyerId,
        @NotBlank String paymentMethodToken,
        @NotBlank String currency,
        @Positive BigDecimal amount,
        String ipAddress,
        String cardFingerprint) {
}