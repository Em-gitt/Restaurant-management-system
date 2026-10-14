package com.unipoli.delivery.dto;

import java.math.BigDecimal;

public record InternalOrderCreatedResponse(
        Long id,
        String orderNumber,
        String status,
        BigDecimal total
) {
}
