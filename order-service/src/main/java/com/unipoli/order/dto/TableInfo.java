package com.unipoli.order.dto;

public record TableInfo(
        Long id,
        String tableNumber,
        Integer capacity,
        String status,
        String currentOrderId
) {
}
