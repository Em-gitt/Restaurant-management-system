package com.unipoli.order.dto;

import java.math.BigDecimal;

public record DishInfo(
        Long dishId,
        String name,
        String description,
        Long categoryId,
        String category,
        BigDecimal price,
        Boolean available
) {
}
