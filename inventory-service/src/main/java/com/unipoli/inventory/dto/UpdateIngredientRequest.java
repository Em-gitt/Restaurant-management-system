package com.unipoli.inventory.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateIngredientRequest(
        @Size(max = 150, message = "Name must not exceed 150 characters")
        String name,

        @Size(max = 50, message = "Unit of measure must not exceed 50 characters")
        String unitOfMeasure,

        @DecimalMin(value = "0.0", message = "Minimum stock must be greater than or equal to 0")
        BigDecimal minimumStock,

        @DecimalMin(value = "0.0", message = "Unit cost must be greater than or equal to 0")
        BigDecimal unitCost
) {
}
