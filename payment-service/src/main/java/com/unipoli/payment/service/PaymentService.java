package com.unipoli.payment.service;

import com.unipoli.payment.client.OrderServiceClient;
import com.unipoli.payment.dto.*;
import com.unipoli.payment.exception.DuplicatePaymentException;
import com.unipoli.payment.exception.PaymentProcessingException;
import com.unipoli.payment.exception.ResourceNotFoundException;
import com.unipoli.payment.model.*;
import com.unipoli.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

@Service
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderServiceClient orderServiceClient;

    public PaymentService(PaymentRepository paymentRepository, OrderServiceClient orderServiceClient) {
        this.paymentRepository = paymentRepository;
        this.orderServiceClient = orderServiceClient;
    }

    public PaymentResponse processPayment(ProcessPaymentRequest request, String authHeader) {
        // 1. Prevent duplicate approved payments for the same order
        if (paymentRepository.existsByOrderIdAndStatus(request.orderId(), PaymentStatus.APPROVED)) {
            throw new DuplicatePaymentException("Order " + request.orderId() + " already has an approved payment");
        }

        // 2. Verify order with Order Service
        OrderVerificationResponse orderInfo = orderServiceClient.verifyOrder(request.orderId(), authHeader);
        if ("CANCELLED".equalsIgnoreCase(orderInfo.status())) {
            throw new PaymentProcessingException("Cannot pay for a cancelled order");
        }

        BigDecimal tip = request.tip() != null ? request.tip() : BigDecimal.ZERO;
        BigDecimal totalPaid = request.amount().add(tip);

        // 3. Process payment method specifics (simulated processing)
        BigDecimal cashTendered = null;
        BigDecimal changeGiven = null;
        String maskedCard = null;
        String cardBrand = request.cardBrand();
        String authCode = null;

        if (request.paymentMethod() == PaymentMethod.CASH) {
            if (request.cashTendered() == null || request.cashTendered().compareTo(totalPaid) < 0) {
                throw new PaymentProcessingException("Cash tendered (" + request.cashTendered() +
                        ") is insufficient to cover total amount (" + totalPaid + ")");
            }
            cashTendered = request.cashTendered();
            changeGiven = cashTendered.subtract(totalPaid);
        } else if (request.paymentMethod() == PaymentMethod.CREDIT_CARD || request.paymentMethod() == PaymentMethod.DEBIT_CARD) {
            String rawCard = request.rawCardNumber();
            if (rawCard != null && rawCard.length() >= 4) {
                String last4 = rawCard.substring(rawCard.length() - 4);
                maskedCard = "**** **** **** " + last4;
            } else {
                maskedCard = "**** **** **** 4242";
            }
            if (cardBrand == null || cardBrand.isBlank()) {
                cardBrand = "VISA";
            }
            authCode = "AUTH-" + (100000 + new Random().nextInt(900000));
        } else if (request.paymentMethod() == PaymentMethod.DELIVERY_APP) {
            authCode = "DELIVERY-APP-PREPAID";
        } else if (request.paymentMethod() == PaymentMethod.TRANSFER) {
            authCode = "SPEI-" + (10000000 + new Random().nextInt(90000000));
        }

        // 4. Generate transaction identifiers
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int rand = 1000 + new Random().nextInt(9000);
        String txnNumber = "TXN-" + timestamp + "-" + rand;
        String receiptNumber = "REC-" + timestamp + "-" + rand;

        Payment payment = new Payment();
        payment.setOrderId(request.orderId());
        payment.setTransactionNumber(txnNumber);
        payment.setPaymentMethod(request.paymentMethod());
        payment.setStatus(PaymentStatus.APPROVED);
        payment.setAmount(request.amount());
        payment.setTip(tip);
        payment.setTotalPaid(totalPaid);
        payment.setCashTendered(cashTendered);
        payment.setChangeGiven(changeGiven);
        payment.setCurrency("MXN");
        payment.setMaskedCardNumber(maskedCard);
        payment.setCardBrand(cardBrand);
        payment.setAuthorizationCode(authCode);
        payment.setReceiptNumber(receiptNumber);
        payment.setNotes(request.notes());

        Payment saved = paymentRepository.save(payment);

        // 5. Notify Order Service of approved payment
        orderServiceClient.notifyPaymentCompleted(
                saved.getOrderId(),
                saved.getTransactionNumber(),
                saved.getPaymentMethod().name(),
                saved.getTotalPaid()
        );

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> findAll(PaymentStatus status) {
        List<Payment> list;
        if (status != null) {
            list = paymentRepository.findByStatus(status);
        } else {
            list = paymentRepository.findAllByOrderByCreatedAtDesc();
        }
        return list.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PaymentResponse findById(Long id) {
        return paymentRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> findByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId).stream()
                .map(this::toResponse)
                .toList();
    }

    public PaymentResponse refundPayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));

        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            throw new PaymentProcessingException("Payment has already been refunded");
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        Payment saved = paymentRepository.save(payment);
        return toResponse(saved);
    }

    public PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getTransactionNumber(),
                payment.getPaymentMethod(),
                payment.getStatus(),
                payment.getAmount(),
                payment.getTip(),
                payment.getTotalPaid(),
                payment.getCashTendered(),
                payment.getChangeGiven(),
                payment.getCurrency(),
                payment.getMaskedCardNumber(),
                payment.getCardBrand(),
                payment.getAuthorizationCode(),
                payment.getReceiptNumber(),
                payment.getNotes(),
                payment.getCreatedAt()
        );
    }
}
