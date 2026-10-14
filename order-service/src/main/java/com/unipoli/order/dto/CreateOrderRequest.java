package com.unipoli.order.dto;

import com.unipoli.order.model.OrderType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record CreateOrderRequest(
        @NotNull(message = "Order type is required")
        OrderType orderType,

        Long tableId,

        Long waiterId,

        @Size(max = 150, message = "Customer name must not exceed 150 characters")
        String customerName,

        @Size(max = 50, message = "Customer phone must not exceed 50 characters")
        String customerPhone,

        @Size(max = 255, message = "Delivery address must not exceed 255 characters")
        String deliveryAddress,

        BigDecimal tip,

        @Size(max = 500, message = "Notes must not exceed 500 characters")
        String notes,

        @NotEmpty(message = "Order must contain at least one item")
        @Valid
        List<CreateOrderItemRequest> items
) {
    public record CreateOrderItemRequest(
            @NotNull(message = "Dish ID is required")
            Long dishId,

            @NotNull(message = "Quantity is required")
            @Positive(message = "Quantity must be greater than 0")
            Integer quantity,

            @Size(max = 255, message = "Special instructions must not exceed 255 characters")
            String specialInstructions
    ) {}
}
