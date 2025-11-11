package com.automatch.payments.domain.repository;

import com.automatch.payments.domain.model.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    Optional<PaymentTransaction> findByRequestId(String requestId);

    List<PaymentTransaction> findByOrderId(String orderId);
}