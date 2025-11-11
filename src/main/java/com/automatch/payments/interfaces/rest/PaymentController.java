package com.automatch.payments.interfaces.rest;

import com.automatch.payments.application.commands.ProcessPaymentCommand;
import com.automatch.payments.application.commands.RefundPaymentCommand;
import com.automatch.payments.application.dtos.PaymentRequestDTO;
import com.automatch.payments.application.dtos.PaymentResponseDTO;
import com.automatch.payments.application.dtos.PaymentViewDTO;
import com.automatch.payments.application.dtos.RefundRequestDTO;
import com.automatch.payments.application.services.PaymentApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentApplicationService service;

    public PaymentController(PaymentApplicationService service) {
        this.service = service;
    }

    // -------------------------------------------------------------
    // POST /api/v1/payments → procesar pago con ejemplo completo
    // -------------------------------------------------------------
    @Operation(summary = "Procesa un pago nuevo", description = "Ejecuta un pago simulando la autorización, captura y validación antifraude.", requestBody = @RequestBody(required = true, description = "Datos de pago", content = @Content(schema = @Schema(implementation = PaymentRequestDTO.class), examples = @ExampleObject(name = "Ejemplo de pago con tarjeta Visa", value = """
            {
              "requestId": "REQ-10001",
              "orderId": "ORD-2025-001",
              "buyerId": "USR-00123",
              "paymentMethodToken": "tok_visa_001",
              "currency": "PEN",
              "amount": 149.90,
              "ipAddress": "190.42.85.77",
              "cardFingerprint": "fp-visa-8732"
            }
            """))))
    @ApiResponse(responseCode = "200", description = "Pago procesado correctamente")
    @PostMapping
    public ResponseEntity<PaymentResponseDTO> process(
            @Valid @org.springframework.web.bind.annotation.RequestBody PaymentRequestDTO dto,
            @Parameter(description = "Identificador único de solicitud", example = "REQ-10001") @RequestHeader(value = "X-Request-Id", required = false) String reqHeader) {

        var requestId = dto.requestId() != null ? dto.requestId() : reqHeader;
        var cmd = new ProcessPaymentCommand(
                requestId,
                dto.orderId(),
                dto.buyerId(),
                dto.ipAddress(),
                dto.cardFingerprint(),
                dto.currency(),
                dto.amount(),
                dto.paymentMethodToken());

        return ResponseEntity.ok(service.process(cmd));
    }

    // -------------------------------------------------------------
    // POST /api/v1/payments/refund → ejemplo con datos reales
    // -------------------------------------------------------------
    @Operation(summary = "Procesa un reembolso de pago", description = "Simula el reembolso de un pago previamente capturado.", requestBody = @RequestBody(required = true, description = "Datos del reembolso", content = @Content(schema = @Schema(implementation = RefundRequestDTO.class), examples = @ExampleObject(name = "Ejemplo de reembolso parcial", value = """
            {
              "requestId": "REQ-10003",
              "orderId": "ORD-2025-003",
              "pspChargeId": "ch_test_REQ-10001",
              "amount": 45.00,
              "currency": "PEN"
            }
            """))))
    @ApiResponse(responseCode = "200", description = "Reembolso procesado correctamente")
    @PostMapping("/refund")
    public ResponseEntity<PaymentResponseDTO> refund(
            @Valid @org.springframework.web.bind.annotation.RequestBody RefundRequestDTO dto) {
        var cmd = new RefundPaymentCommand(
                dto.requestId(),
                dto.orderId(),
                dto.pspChargeId(),
                dto.amount(),
                dto.currency());
        return ResponseEntity.ok(service.refund(cmd));
    }

    // -------------------------------------------------------------
    // GET /api/v1/payments/request/{requestId}
    // -------------------------------------------------------------
    @Operation(summary = "Obtiene un pago por requestId")
    @GetMapping("/request/{requestId}")
    public ResponseEntity<PaymentResponseDTO> getByRequestId(
            @Parameter(description = "Identificador único del pago", example = "REQ-10001") @PathVariable String requestId) {
        var tx = service.findByRequestId(requestId);
        return tx != null ? ResponseEntity.ok(tx) : ResponseEntity.notFound().build();
    }

    // -------------------------------------------------------------
    // GET /api/v1/payments/order/{orderId}
    // -------------------------------------------------------------
    @Operation(summary = "Obtiene todos los pagos asociados a un orderId")
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<PaymentViewDTO>> getByOrderId(
            @Parameter(description = "ID del pedido asociado al pago", example = "ORD-2025-001") @PathVariable String orderId) {
        return ResponseEntity.ok(service.findByOrderId(orderId));
    }
}