package com.unipoli.report.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public record SalesReportResponse(
        LocalDateTime periodStart,
        LocalDateTime periodEnd,
        long totalOrders,
        BigDecimal totalSales,
        BigDecimal totalSubtotal,
        BigDecimal totalTax,
        BigDecimal totalTips,
        BigDecimal totalCommissions,
        BigDecimal netRevenue,
        BigDecimal averageTicket,
        Map<String, BigDecimal> salesByOrderType,
        Map<String, BigDecimal> salesByPlatform
) {
}
