package com.unipoli.inventory.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RecipeItemRequest(
        @NotNull(message = "Ingredient ID is required")
        Long ingredientId,

        @NotNull(message = "Quantity required must be provided")
        @DecimalMin(value = "0.001", message = "Quantity must be greater than 0")
        BigDecimal quantityRequired
) {
}
