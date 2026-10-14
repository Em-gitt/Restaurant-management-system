package com.unipoli.delivery.dto;

import com.unipoli.delivery.model.DeliveryStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateDeliveryStatusRequest(
        @NotNull(message = "Delivery status is required")
        DeliveryStatus status
) {
}
