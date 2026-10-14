package com.unipoli.inventory.dto;

import java.math.BigDecimal;
import java.util.List;

public record DishRecipeResponse(
        Long dishId,
        List<RecipeItemResponse> items
) {
    public record RecipeItemResponse(
            Long id,
            Long ingredientId,
            String ingredientName,
            String unitOfMeasure,
            BigDecimal quantityRequired
    ) {}
}
