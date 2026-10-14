package com.unipoli.payment.service;

import com.unipoli.payment.client.OrderServiceClient;
import com.unipoli.payment.dto.OrderVerificationResponse;
import com.unipoli.payment.dto.PaymentResponse;
import com.unipoli.payment.dto.ProcessPaymentRequest;
import com.unipoli.payment.exception.DuplicatePaymentException;
import com.unipoli.payment.exception.PaymentProcessingException;
import com.unipoli.payment.model.Payment;
import com.unipoli.payment.model.PaymentMethod;
import com.unipoli.payment.model.PaymentStatus;
import com.unipoli.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderServiceClient orderServiceClient;

    @InjectMocks
    private PaymentService paymentService;

    private OrderVerificationResponse orderInfo;

    @BeforeEach
    void setUp() {
        orderInfo = new OrderVerificationResponse(100L, "ORD-100", "CONFIRMED", new BigDecimal("250.00"));
    }

    @Test
    void processPayment_WhenCashAndSufficientTendered_ShouldCalculateChange() {
        ProcessPaymentRequest request = new ProcessPaymentRequest(
                100L,
                PaymentMethod.CASH,
                new BigDecimal("250.00"),
                new BigDecimal("25.00"),
                new BigDecimal("300.00"),
                null,
                null,
                "Cash payment"
        );

        when(paymentRepository.existsByOrderIdAndStatus(100L, PaymentStatus.APPROVED)).thenReturn(false);
        when(orderServiceClient.verifyOrder(eq(100L), any())).thenReturn(orderInfo);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> {
            Payment p = i.getArgument(0);
            p.setId(1L);
            p.setCreatedAt(LocalDateTime.now());
            return p;
        });

        PaymentResponse response = paymentService.processPayment(request, "Bearer token");

        assertNotNull(response);
        assertEquals(PaymentStatus.APPROVED, response.status());
        assertEquals(new BigDecimal("250.00"), response.amount());
        assertEquals(new BigDecimal("25.00"), response.tip());
        assertEquals(new BigDecimal("275.00"), response.totalPaid());
        assertEquals(new BigDecimal("300.00"), response.cashTendered());
        // Change = 300 - 275 = 25.00
        assertEquals(new BigDecimal("25.00"), response.changeGiven());

        verify(orderServiceClient, times(1)).notifyPaymentCompleted(eq(100L), any(), eq("CASH"), eq(new BigDecimal("275.00")));
    }

    @Test
    void processPayment_WhenCreditCard_ShouldMaskCardAndGenerateAuthCode() {
        ProcessPaymentRequest request = new ProcessPaymentRequest(
                100L,
                PaymentMethod.CREDIT_CARD,
                new BigDecimal("250.00"),
                BigDecimal.ZERO,
                null,
                "4111222233334567",
                "VISA",
                "Card payment"
        );

        when(paymentRepository.existsByOrderIdAndStatus(100L, PaymentStatus.APPROVED)).thenReturn(false);
        when(orderServiceClient.verifyOrder(eq(100L), any())).thenReturn(orderInfo);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> {
            Payment p = i.getArgument(0);
            p.setId(2L);
            p.setCreatedAt(LocalDateTime.now());
            return p;
        });

        PaymentResponse response = paymentService.processPayment(request, "Bearer token");

        assertNotNull(response);
        assertEquals(PaymentStatus.APPROVED, response.status());
        assertEquals("**** **** **** 4567", response.maskedCardNumber());
        assertNotNull(response.authorizationCode());
        assertTrue(response.authorizationCode().startsWith("AUTH-"));
    }

    @Test
    void processPayment_WhenDuplicateApprovedPayment_ShouldThrowDuplicatePaymentException() {
        ProcessPaymentRequest request = new ProcessPaymentRequest(
                100L,
                PaymentMethod.CASH,
                new BigDecimal("250.00"),
                BigDecimal.ZERO,
                new BigDecimal("250.00"),
                null,
                null,
                null
        );

        when(paymentRepository.existsByOrderIdAndStatus(100L, PaymentStatus.APPROVED)).thenReturn(true);

        assertThrows(DuplicatePaymentException.class, () -> paymentService.processPayment(request, "Bearer token"));
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void processPayment_WhenCashTenderedInsufficient_ShouldThrowException() {
        ProcessPaymentRequest request = new ProcessPaymentRequest(
                100L,
                PaymentMethod.CASH,
                new BigDecimal("250.00"),
                BigDecimal.ZERO,
                new BigDecimal("200.00"), // Insufficient!
                null,
                null,
                null
        );

        when(paymentRepository.existsByOrderIdAndStatus(100L, PaymentStatus.APPROVED)).thenReturn(false);
        when(orderServiceClient.verifyOrder(eq(100L), any())).thenReturn(orderInfo);

        assertThrows(PaymentProcessingException.class, () -> paymentService.processPayment(request, "Bearer token"));
    }

    @Test
    void refundPayment_WhenApproved_ShouldSetRefunded() {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setStatus(PaymentStatus.APPROVED);

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        PaymentResponse response = paymentService.refundPayment(1L);

        assertEquals(PaymentStatus.REFUNDED, response.status());
    }
}
