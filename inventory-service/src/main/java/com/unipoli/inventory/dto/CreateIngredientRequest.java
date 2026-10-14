package com.unipoli.inventory.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateIngredientRequest(
        @NotBlank(message = "Ingredient name is required")
        @Size(max = 150, message = "Name must not exceed 150 characters")
        String name,

        @NotBlank(message = "Unit of measure is required")
        @Size(max = 50, message = "Unit of measure must not exceed 50 characters")
        String unitOfMeasure,

        @NotNull(message = "Initial stock is required")
        @DecimalMin(value = "0.0", message = "Initial stock must be greater than or equal to 0")
        BigDecimal initialStock,

        @NotNull(message = "Minimum stock is required")
        @DecimalMin(value = "0.0", message = "Minimum stock must be greater than or equal to 0")
        BigDecimal minimumStock,

        @NotNull(message = "Unit cost is required")
        @DecimalMin(value = "0.0", message = "Unit cost must be greater than or equal to 0")
        BigDecimal unitCost
) {
}
