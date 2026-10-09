package com.unipoli.menu.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateDishRequest(

        @NotBlank
        String name,

        String description,

        @NotNull
        @Positive
        Long categoryId,

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal price,

        Boolean available
) {
}
