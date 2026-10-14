package com.unipoli.payment.dto;

import java.math.BigDecimal;

public record OrderVerificationResponse(
        Long id,
        String orderNumber,
        String status,
        BigDecimal total
) {
}
