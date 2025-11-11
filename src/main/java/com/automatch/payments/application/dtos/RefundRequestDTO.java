package com.automatch.payments.application.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record RefundRequestDTO(
        @NotBlank String requestId,
        @NotBlank String orderId,
        @NotBlank String pspChargeId,
        @NotNull BigDecimal amount,
        @NotBlank String currency) {
}