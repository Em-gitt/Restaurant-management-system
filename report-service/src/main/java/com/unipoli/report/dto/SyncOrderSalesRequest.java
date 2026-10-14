package com.unipoli.report.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SyncOrderSalesRequest(
        @NotNull(message = "Order ID is required")
        Long orderId,

        @NotBlank(message = "Order number is required")
        String orderNumber,

        @NotBlank(message = "Order type is required")
        String orderType,

        String platform,

        @NotBlank(message = "Order status is required")
        String orderStatus,

        @NotNull(message = "Subtotal is required")
        BigDecimal subtotal,

        @NotNull(message = "Tax is required")
        BigDecimal tax,

        BigDecimal tip,

        @NotNull(message = "Total is required")
        BigDecimal total,

        BigDecimal commissionAmount,

        LocalDateTime orderDate
) {
}
