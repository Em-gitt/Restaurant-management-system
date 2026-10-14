package com.unipoli.inventory.dto;

import com.unipoli.inventory.model.MovementType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovementResponse(
        Long id,
        Long ingredientId,
        String ingredientName,
        MovementType movementType,
        BigDecimal quantity,
        BigDecimal previousStock,
        BigDecimal newStock,
        String referenceId,
        String reason,
        LocalDateTime createdAt
) {
}
