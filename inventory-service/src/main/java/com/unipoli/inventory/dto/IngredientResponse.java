package com.unipoli.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record IngredientResponse(
        Long id,
        String name,
        String unitOfMeasure,
        BigDecimal currentStock,
        BigDecimal minimumStock,
        BigDecimal unitCost,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
