package com.unipoli.payment.dto;

import com.unipoli.payment.model.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProcessPaymentRequest(
        @NotNull(message = "Order ID is required")
        Long orderId,

        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        BigDecimal amount,

        @DecimalMin(value = "0.00", message = "Tip cannot be negative")
        BigDecimal tip,

        BigDecimal cashTendered,

        @Size(max = 20, message = "Raw card number must not exceed 20 characters")
        String rawCardNumber,

        @Size(max = 50, message = "Card brand must not exceed 50 characters")
        String cardBrand,

        @Size(max = 255, message = "Notes must not exceed 255 characters")
        String notes
) {
}
