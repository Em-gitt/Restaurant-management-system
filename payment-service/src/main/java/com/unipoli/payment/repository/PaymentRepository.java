package com.unipoli.payment.repository;

import com.unipoli.payment.model.Payment;
import com.unipoli.payment.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByTransactionNumber(String transactionNumber);

    List<Payment> findByOrderId(Long orderId);

    boolean existsByOrderIdAndStatus(Long orderId, PaymentStatus status);

    List<Payment> findByStatus(PaymentStatus status);

    List<Payment> findAllByOrderByCreatedAtDesc();
}
