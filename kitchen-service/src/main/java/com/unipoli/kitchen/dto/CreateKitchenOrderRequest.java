package com.unipoli.kitchen.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record CreateKitchenOrderRequest(
        @NotNull(message = "Order ID is required")
        Long orderId,

        @NotBlank(message = "Order number is required")
        String orderNumber,

        Long tableId,

        @NotBlank(message = "Order type is required")
        String orderType,

        String notes,

        @NotEmpty(message = "Items list cannot be empty")
        @Valid
        List<KitchenItemRequest> items
) {
    public record KitchenItemRequest(
            @NotNull(message = "Dish ID is required")
            Long dishId,

            @NotBlank(message = "Dish name is required")
            String dishName,

            @NotNull(message = "Quantity is required")
            @Positive(message = "Quantity must be positive")
            Integer quantity,

            String specialInstructions
    ) {}
}
