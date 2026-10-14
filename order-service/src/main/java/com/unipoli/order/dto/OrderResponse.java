package com.unipoli.order.dto;

import com.unipoli.order.model.OrderStatus;
import com.unipoli.order.model.OrderType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        String orderNumber,
        OrderType orderType,
        OrderStatus status,
        Long tableId,
        Long waiterId,
        String customerName,
        String customerPhone,
        String deliveryAddress,
        BigDecimal subtotal,
        BigDecimal tax,
        BigDecimal tip,
        BigDecimal total,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<OrderItemResponse> items
) {
    public record OrderItemResponse(
            Long id,
            Long dishId,
            String dishName,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal,
            String specialInstructions
    ) {}
}
