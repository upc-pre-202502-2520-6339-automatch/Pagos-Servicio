package com.automatch.payments.application.dtos;

public record PaymentResponseDTO(
        String requestId,
        String orderId,
        String status,
        String pspChargeId,
        String message) {
}