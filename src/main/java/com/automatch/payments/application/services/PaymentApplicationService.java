package com.automatch.payments.application.services;

import com.automatch.payments.application.commands.ProcessPaymentCommand;
import com.automatch.payments.application.commands.RefundPaymentCommand;
import com.automatch.payments.application.dtos.PaymentResponseDTO;
import com.automatch.payments.application.dtos.PaymentViewDTO;
import com.automatch.payments.domain.events.*;
import com.automatch.payments.domain.model.*;
import com.automatch.payments.domain.repository.PaymentTransactionRepository;
import com.automatch.payments.domain.services.AntiFraudService;
import com.automatch.payments.infrastructure.messaging.PaymentEventPublisher;
import com.automatch.payments.infrastructure.outbox.OutboxEventRepository;
import com.automatch.payments.infrastructure.psp.PSPClient;
import com.automatch.payments.infrastructure.sunat.SunatInvoiceClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.automatch.payments.infrastructure.outbox.OutboxEvent;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.stream.Collectors;
import java.util.List;

@Service
public class PaymentApplicationService {

    private final PaymentTransactionRepository repo;
    private final AntiFraudService antiFraud;
    private final PSPClient psp;
    private final PaymentEventPublisher publisher;
    private final SunatInvoiceClient sunat;
    private final OutboxEventRepository outbox;
    private final ObjectMapper mapper;

    public PaymentApplicationService(PaymentTransactionRepository repo, AntiFraudService antiFraud,
            PSPClient psp, PaymentEventPublisher publisher, SunatInvoiceClient sunat, OutboxEventRepository outbox,
            ObjectMapper mapper) {
        this.repo = repo;
        this.antiFraud = antiFraud;
        this.psp = psp;
        this.publisher = publisher;
        this.sunat = sunat;
        this.outbox = outbox;
        this.mapper = mapper;
    }

    @Transactional
    public PaymentResponseDTO process(ProcessPaymentCommand cmd) {
        // Idempotencia
        var existing = repo.findByRequestId(cmd.requestId());
        if (existing.isPresent()) {
            var tx = existing.get();
            return new PaymentResponseDTO(tx.getRequestId(), tx.getOrderId(), tx.getStatus().name(),
                    tx.getPspChargeId(), "Idempotent replay");
        }

        var tx = new PaymentTransaction();
        tx.setRequestId(cmd.requestId());
        tx.setOrderId(cmd.orderId());
        tx.setMethod(PaymentMethod.CARD); // simplificado, podrías mapear desde token
        tx.setCurrency(cmd.currency());
        tx.setAmount(cmd.amount());
        tx.setStatus(PaymentStatus.PENDING);
        repo.save(tx);

        // Antifraude previo al cobro
        var money = Money.of(cmd.amount(), cmd.currency());
        var fraud = antiFraud.preAuthorize(cmd.buyerId(), cmd.cardFingerprint(), money, cmd.ipAddress());
        if (fraud.suspicious()) {
            tx.setStatus(PaymentStatus.FRAUD_SUSPECT);
            tx.setReason(fraud.reason());
            repo.save(tx);
            publisher.publish(new FraudAlertEvent(cmd.requestId(), cmd.orderId(), fraud.reason(),
                    cmd.currency(), cmd.amount().toPlainString(), OffsetDateTime.now()));
            return new PaymentResponseDTO(cmd.requestId(), cmd.orderId(), tx.getStatus().name(), null,
                    "Fraud suspected: " + fraud.reason());
        }

        // Cobro en PSP (autorización + captura)
        var charge = psp.charge(cmd.paymentMethodToken(), cmd.currency(), cmd.amount(), cmd.orderId(), cmd.requestId());
        if (!charge.success()) {
            tx.setStatus(PaymentStatus.FAILED);
            tx.setReason(charge.errorMessage());
            repo.save(tx);
            return new PaymentResponseDTO(cmd.requestId(), cmd.orderId(), tx.getStatus().name(), null,
                    charge.errorMessage());
        }

        tx.setStatus(PaymentStatus.CAPTURED);
        tx.setReason("Payment successful");
        tx.setPspChargeId(charge.chargeId());
        repo.save(tx);

        // Emisión comprobante (SUNAT) – modo fire-and-forget tolerante a fallas
        try {
            sunat.issueElectronicReceipt(tx.getOrderId(), tx.getCurrency(), tx.getAmount(), charge.chargeId());
        } catch (Exception e) {
            // registra y deja que un job reintente
        }

        // Evento
        var evt = new PaymentConfirmedEvent(
                cmd.requestId(),
                cmd.orderId(),
                charge.chargeId(),
                cmd.currency(),
                cmd.amount().toPlainString(),
                OffsetDateTime.now());
        saveOutbox("payment", cmd.orderId(), "PaymentConfirmedEvent", evt);

        return new PaymentResponseDTO(
                cmd.requestId(),
                cmd.orderId(),
                tx.getStatus().name(),
                charge.chargeId(),
                "Payment processed successfully for order " + cmd.orderId());
    }

    @Transactional
    public PaymentResponseDTO refund(RefundPaymentCommand cmd) {

        var tx = repo.findByRequestId(cmd.requestId()).orElseGet(() -> {
            var t = new PaymentTransaction();
            t.setRequestId(cmd.requestId());
            t.setOrderId(cmd.orderId());
            t.setCurrency(cmd.currency());
            t.setAmount(cmd.amount());
            t.setMethod(PaymentMethod.CARD);
            t.setStatus(PaymentStatus.PENDING);
            repo.save(t);
            return t;
        });

        var pspResp = psp.refund(cmd.pspChargeId(), cmd.currency(), cmd.amount(), cmd.requestId());

        if (!pspResp.success()) {
            tx.setStatus(PaymentStatus.FAILED);
            tx.setReason(pspResp.errorMessage());
            repo.save(tx);
            return new PaymentResponseDTO(cmd.requestId(), cmd.orderId(),
                    tx.getStatus().name(), tx.getPspChargeId(), pspResp.errorMessage());
        }

        tx.setStatus(PaymentStatus.REFUNDED);
        tx.setPspChargeId(cmd.pspChargeId());
        repo.save(tx);

        publisher.publish(new RefundedEvent(cmd.requestId(), cmd.orderId(),
                cmd.pspChargeId(), cmd.currency(), cmd.amount().toPlainString(), OffsetDateTime.now()));

        return new PaymentResponseDTO(cmd.requestId(), cmd.orderId(),
                tx.getStatus().name(), cmd.pspChargeId(), "Refunded");
    }

    public PaymentResponseDTO findByRequestId(String requestId) {
        return repo.findByRequestId(requestId)
                .map(tx -> new PaymentResponseDTO(
                        tx.getRequestId(),
                        tx.getOrderId(),
                        tx.getStatus().name(),
                        tx.getPspChargeId(),
                        tx.getReason() != null ? tx.getReason() : "Payment processed successfully"))
                .orElse(null);
    }

    public List<PaymentViewDTO> findByOrderId(String orderId) {
        return repo.findByOrderId(orderId).stream()
                .map(tx -> new PaymentViewDTO(
                        tx.getRequestId(),
                        tx.getOrderId(),
                        tx.getStatus().name(),
                        tx.getPspChargeId(),
                        tx.getCurrency(),
                        tx.getAmount(),
                        tx.getCreatedAt(),
                        tx.getReason()))
                .collect(Collectors.toList());
    }

    private void saveOutbox(String aggType, String aggId, String type, Object payload) {
        try {
            var out = new OutboxEvent();
            out.setAggregateType(aggType);
            out.setAggregateId(aggId);
            out.setType(type);
            out.setPayload(mapper.writeValueAsString(payload));
            outbox.save(out);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}