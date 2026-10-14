package com.unipoli.inventory.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record DeductOrderInventoryRequest(
        @NotNull(message = "Order ID is required")
        Long orderId,

        @NotEmpty(message = "Order items list must not be empty")
        @Valid
        List<OrderItemDeduction> items
) {
    public record OrderItemDeduction(
            @NotNull(message = "Dish ID is required")
            Long dishId,

            @NotNull(message = "Quantity is required")
            @Positive(message = "Quantity must be positive")
            Integer quantity
    ) {}
}
