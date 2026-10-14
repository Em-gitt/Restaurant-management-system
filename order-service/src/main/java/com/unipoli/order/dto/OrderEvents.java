package com.unipoli.order.dto;

import com.unipoli.order.model.OrderStatus;
import com.unipoli.order.model.OrderType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderEvents {

    public record OrderCreatedEvent(
            Long orderId,
            String orderNumber,
            OrderType orderType,
            OrderStatus status,
            Long tableId,
            Long waiterId,
            BigDecimal total,
            String notes,
            LocalDateTime createdAt,
            List<OrderItemEvent> items
    ) {}

    public record OrderItemEvent(
            Long dishId,
            String dishName,
            Integer quantity,
            BigDecimal unitPrice,
            String specialInstructions
    ) {}

    public record OrderReadyEvent(
            Long orderId,
            String orderNumber,
            LocalDateTime readyAt
    ) {}

    public record PaymentCompletedEvent(
            Long orderId,
            String transactionNumber,
            String paymentMethod,
            BigDecimal amount,
            LocalDateTime paidAt
    ) {}
}
