package com.unipoli.report.service;

import com.unipoli.report.client.InventoryServiceClient;
import com.unipoli.report.dto.InventoryReportResponse;
import com.unipoli.report.dto.SalesReportResponse;
import com.unipoli.report.dto.SyncOrderSalesRequest;
import com.unipoli.report.model.SalesProjection;
import com.unipoli.report.repository.SalesProjectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ReportService {

    private final SalesProjectionRepository salesRepository;
    private final InventoryServiceClient inventoryClient;

    public ReportService(SalesProjectionRepository salesRepository, InventoryServiceClient inventoryClient) {
        this.salesRepository = salesRepository;
        this.inventoryClient = inventoryClient;
    }

    public SalesProjection syncOrderSales(SyncOrderSalesRequest request) {
        SalesProjection projection = salesRepository.findByOrderId(request.orderId())
                .orElseGet(() -> {
                    SalesProjection sp = new SalesProjection();
                    sp.setOrderId(request.orderId());
                    return sp;
                });

        projection.setOrderNumber(request.orderNumber());
        projection.setOrderType(request.orderType());
        projection.setPlatform(request.platform() != null && !request.platform().isBlank() ? request.platform() : "DIRECT");
        projection.setOrderStatus(request.orderStatus());
        projection.setSubtotal(request.subtotal() != null ? request.subtotal() : BigDecimal.ZERO);
        projection.setTax(request.tax() != null ? request.tax() : BigDecimal.ZERO);
        projection.setTip(request.tip() != null ? request.tip() : BigDecimal.ZERO);
        projection.setTotal(request.total() != null ? request.total() : BigDecimal.ZERO);

        BigDecimal commission = request.commissionAmount() != null ? request.commissionAmount() : BigDecimal.ZERO;
        projection.setCommissionAmount(commission);
        projection.setNetRevenue(projection.getTotal().subtract(commission));

        if (request.orderDate() != null) {
            projection.setOrderDate(request.orderDate());
        } else if (projection.getOrderDate() == null) {
            projection.setOrderDate(LocalDateTime.now());
        }

        return salesRepository.save(projection);
    }

    @Transactional(readOnly = true)
    public SalesReportResponse getSalesReport(LocalDateTime periodStart, LocalDateTime periodEnd) {
        LocalDateTime start = periodStart != null ? periodStart : LocalDateTime.now().minusDays(30);
        LocalDateTime end = periodEnd != null ? periodEnd : LocalDateTime.now();

        List<SalesProjection> projections = salesRepository.findByOrderDateBetween(start, end);

        long totalOrders = projections.size();
        BigDecimal totalSales = BigDecimal.ZERO;
        BigDecimal totalSubtotal = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;
        BigDecimal totalTips = BigDecimal.ZERO;
        BigDecimal totalCommissions = BigDecimal.ZERO;
        BigDecimal netRevenue = BigDecimal.ZERO;

        Map<String, BigDecimal> salesByOrderType = new HashMap<>();
        Map<String, BigDecimal> salesByPlatform = new HashMap<>();

        for (SalesProjection sp : projections) {
            BigDecimal orderTotal = sp.getTotal() != null ? sp.getTotal() : BigDecimal.ZERO;
            BigDecimal orderSubtotal = sp.getSubtotal() != null ? sp.getSubtotal() : BigDecimal.ZERO;
            BigDecimal orderTax = sp.getTax() != null ? sp.getTax() : BigDecimal.ZERO;
            BigDecimal orderTip = sp.getTip() != null ? sp.getTip() : BigDecimal.ZERO;
            BigDecimal orderCommission = sp.getCommissionAmount() != null ? sp.getCommissionAmount() : BigDecimal.ZERO;
            BigDecimal orderNet = sp.getNetRevenue() != null ? sp.getNetRevenue() : orderTotal.subtract(orderCommission);

            totalSales = totalSales.add(orderTotal);
            totalSubtotal = totalSubtotal.add(orderSubtotal);
            totalTax = totalTax.add(orderTax);
            totalTips = totalTips.add(orderTip);
            totalCommissions = totalCommissions.add(orderCommission);
            netRevenue = netRevenue.add(orderNet);

            String orderType = sp.getOrderType() != null ? sp.getOrderType() : "UNKNOWN";
            salesByOrderType.merge(orderType, orderTotal, BigDecimal::add);

            String platform = sp.getPlatform() != null ? sp.getPlatform() : "DIRECT";
            salesByPlatform.merge(platform, orderTotal, BigDecimal::add);
        }

        BigDecimal averageTicket = totalOrders > 0
                ? totalSales.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return new SalesReportResponse(
                start,
                end,
                totalOrders,
                totalSales,
                totalSubtotal,
                totalTax,
                totalTips,
                totalCommissions,
                netRevenue,
                averageTicket,
                salesByOrderType,
                salesByPlatform
        );
    }

    @Transactional(readOnly = true)
    public InventoryReportResponse getInventoryReport(String authHeader) {
        List<InventoryReportResponse.InventoryAlertDto> alerts = inventoryClient.getLowStockAlerts(authHeader);
        return new InventoryReportResponse(alerts.size(), alerts);
    }

    @Transactional(readOnly = true)
    public List<SalesProjection> getAllProjections() {
        return salesRepository.findAllByOrderByOrderDateDesc();
    }
}
