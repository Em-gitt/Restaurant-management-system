package com.unipoli.menu.dto;

import java.math.BigDecimal;

public record DishResponse(
        Long dishId,
        String name,
        String description,
        Long categoryId,
        String category,
        BigDecimal price,
        Boolean available
) {
}
