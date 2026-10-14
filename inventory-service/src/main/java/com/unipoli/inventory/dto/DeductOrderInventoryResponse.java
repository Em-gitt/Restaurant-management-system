package com.unipoli.inventory.dto;

import java.util.List;

public record DeductOrderInventoryResponse(
        Long orderId,
        boolean success,
        boolean alreadyProcessed,
        String message,
        List<DeductionItemResult> deductedIngredients
) {
    public record DeductionItemResult(
            Long ingredientId,
            String ingredientName,
            java.math.BigDecimal quantityDeducted,
            java.math.BigDecimal remainingStock
    ) {}
}
