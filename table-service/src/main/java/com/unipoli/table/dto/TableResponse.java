package com.unipoli.table.dto;

import com.unipoli.table.model.TableStatus;

public record TableResponse(
        Long tableId,
        String tableNumber,
        Integer capacity,
        TableStatus status,
        String currentOrderId
) {
}
