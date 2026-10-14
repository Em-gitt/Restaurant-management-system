package com.unipoli.report.dto;

import java.math.BigDecimal;
import java.util.List;

public record InventoryReportResponse(
        int totalAlerts,
        List<InventoryAlertDto> lowStockAlerts
) {
    public record InventoryAlertDto(
            Long ingredientId,
            String name,
            String unitOfMeasure,
            BigDecimal currentStock,
            BigDecimal minimumStock,
            BigDecimal deficit,
            String alertLevel
    ) {}
}
