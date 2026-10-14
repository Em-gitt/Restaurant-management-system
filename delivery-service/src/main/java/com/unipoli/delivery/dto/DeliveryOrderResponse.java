package com.unipoli.delivery.dto;

import com.unipoli.delivery.model.DeliveryPlatform;
import com.unipoli.delivery.model.DeliveryStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DeliveryOrderResponse(
        Long id,
        DeliveryPlatform platform,
        String externalOrderId,
        Long internalOrderId,
        String customerName,
        String customerPhone,
        String deliveryAddress,
        BigDecimal orderTotal,
        BigDecimal commissionRate,
        BigDecimal commissionAmount,
        BigDecimal netPayout,
        DeliveryStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
