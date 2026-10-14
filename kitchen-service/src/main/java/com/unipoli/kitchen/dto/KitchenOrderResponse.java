package com.unipoli.kitchen.dto;

import com.unipoli.kitchen.model.KitchenStatus;

import java.time.LocalDateTime;
import java.util.List;

public record KitchenOrderResponse(
        Long id,
        Long orderId,
        String orderNumber,
        Long tableId,
        String orderType,
        KitchenStatus status,
        String notes,
        LocalDateTime startedAt,
        LocalDateTime readyAt,
        Integer preparationTimeMinutes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<KitchenItemResponse> items
) {
    public record KitchenItemResponse(
            Long id,
            Long dishId,
            String dishName,
            Integer quantity,
            String specialInstructions
    ) {}
}
