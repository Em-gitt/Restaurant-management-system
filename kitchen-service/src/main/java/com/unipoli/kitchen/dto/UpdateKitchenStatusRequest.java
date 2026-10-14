package com.unipoli.kitchen.dto;

import com.unipoli.kitchen.model.KitchenStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateKitchenStatusRequest(
        @NotNull(message = "Kitchen status is required")
        KitchenStatus status
) {
}
