package com.unipoli.inventory.dto;

import java.math.BigDecimal;

public record InventoryAlertResponse(
        Long ingredientId,
        String name,
        String unitOfMeasure,
        BigDecimal currentStock,
        BigDecimal minimumStock,
        BigDecimal deficit,
        String alertLevel
) {
}
