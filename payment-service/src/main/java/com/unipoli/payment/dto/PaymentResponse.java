package com.unipoli.payment.dto;

import com.unipoli.payment.model.PaymentMethod;
import com.unipoli.payment.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        Long id,
        Long orderId,
        String transactionNumber,
        PaymentMethod paymentMethod,
        PaymentStatus status,
        BigDecimal amount,
        BigDecimal tip,
        BigDecimal totalPaid,
        BigDecimal cashTendered,
        BigDecimal changeGiven,
        String currency,
        String maskedCardNumber,
        String cardBrand,
        String authorizationCode,
        String receiptNumber,
        String notes,
        LocalDateTime createdAt
) {
}
