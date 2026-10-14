package com.unipoli.inventory.dto;

import com.unipoli.inventory.model.MovementType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record RecordMovementRequest(
        @NotNull(message = "Ingredient ID is required")
        Long ingredientId,

        @NotNull(message = "Movement type is required")
        MovementType movementType,

        @NotNull(message = "Quantity is required")
        @DecimalMin(value = "0.001", message = "Quantity must be greater than 0")
        BigDecimal quantity,

        @Size(max = 100, message = "Reference ID must not exceed 100 characters")
        String referenceId,

        @Size(max = 255, message = "Reason must not exceed 255 characters")
        String reason
) {
}
