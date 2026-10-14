package com.unipoli.delivery.dto;

import com.unipoli.delivery.model.DeliveryPlatform;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

public record DeliveryWebhookOrderRequest(
        @NotNull(message = "Platform is required")
        DeliveryPlatform platform,

        @NotBlank(message = "External order ID is required")
        String externalOrderId,

        @NotBlank(message = "Customer name is required")
        String customerName,

        String customerPhone,

        @NotBlank(message = "Delivery address is required")
        String deliveryAddress,

        @NotNull(message = "Order total is required")
        @DecimalMin(value = "0.01", message = "Order total must be positive")
        BigDecimal orderTotal,

        String notes,

        @NotEmpty(message = "Order must have at least one item")
        @Valid
        List<DeliveryOrderItemRequest> items
) {
    public record DeliveryOrderItemRequest(
            @NotNull(message = "Dish ID is required")
            Long dishId,

            @NotNull(message = "Quantity is required")
            @Positive(message = "Quantity must be positive")
            Integer quantity,

            String specialInstructions
    ) {}
}
