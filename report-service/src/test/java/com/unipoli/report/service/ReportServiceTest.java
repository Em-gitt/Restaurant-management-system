package com.unipoli.report.service;

import com.unipoli.report.client.InventoryServiceClient;
import com.unipoli.report.dto.InventoryReportResponse;
import com.unipoli.report.dto.SalesReportResponse;
import com.unipoli.report.dto.SyncOrderSalesRequest;
import com.unipoli.report.model.SalesProjection;
import com.unipoli.report.repository.SalesProjectionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private SalesProjectionRepository salesRepository;

    @Mock
    private InventoryServiceClient inventoryClient;

    @InjectMocks
    private ReportService reportService;

    private SyncOrderSalesRequest dineInRequest;
    private SyncOrderSalesRequest deliveryRequest;

    @BeforeEach
    void setUp() {
        dineInRequest = new SyncOrderSalesRequest(
                100L,
                "ORD-100",
                "DINE_IN",
                "DIRECT",
                "PAID",
                new BigDecimal("500.00"),
                new BigDecimal("80.00"),
                new BigDecimal("50.00"),
                new BigDecimal("630.00"),
                BigDecimal.ZERO,
                LocalDateTime.now()
        );

        deliveryRequest = new SyncOrderSalesRequest(
                101L,
                "ORD-101",
                "DELIVERY",
                "RAPPI",
                "PAID",
                new BigDecimal("400.00"),
                new BigDecimal("64.00"),
                BigDecimal.ZERO,
                new BigDecimal("464.00"),
                new BigDecimal("72.00"),
                LocalDateTime.now()
        );
    }

    @Test
    void syncOrderSales_WhenNewOrder_ShouldCreateAndSave() {
        when(salesRepository.findByOrderId(100L)).thenReturn(Optional.empty());
        when(salesRepository.save(any(SalesProjection.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SalesProjection saved = reportService.syncOrderSales(dineInRequest);

        assertNotNull(saved);
        assertEquals(100L, saved.getOrderId());
        assertEquals("ORD-100", saved.getOrderNumber());
        assertEquals("DINE_IN", saved.getOrderType());
        assertEquals("DIRECT", saved.getPlatform());
        assertEquals(new BigDecimal("630.00"), saved.getTotal());
        assertEquals(BigDecimal.ZERO, saved.getCommissionAmount());
        assertEquals(new BigDecimal("630.00"), saved.getNetRevenue());

        verify(salesRepository).save(any(SalesProjection.class));
    }

    @Test
    void syncOrderSales_WhenExistingOrder_ShouldUpdate() {
        SalesProjection existing = new SalesProjection();
        existing.setOrderId(100L);
        existing.setOrderNumber("ORD-100");
        existing.setOrderStatus("PENDING");
        existing.setTotal(new BigDecimal("500.00"));

        when(salesRepository.findByOrderId(100L)).thenReturn(Optional.of(existing));
        when(salesRepository.save(any(SalesProjection.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SalesProjection updated = reportService.syncOrderSales(dineInRequest);

        assertEquals("PAID", updated.getOrderStatus());
        assertEquals(new BigDecimal("630.00"), updated.getTotal());
        verify(salesRepository).save(existing);
    }

    @Test
    void getSalesReport_ShouldCalculateAggregatesCorrectly() {
        SalesProjection sp1 = new SalesProjection();
        sp1.setOrderId(100L);
        sp1.setOrderNumber("ORD-100");
        sp1.setOrderType("DINE_IN");
        sp1.setPlatform("DIRECT");
        sp1.setSubtotal(new BigDecimal("500.00"));
        sp1.setTax(new BigDecimal("80.00"));
        sp1.setTip(new BigDecimal("50.00"));
        sp1.setTotal(new BigDecimal("630.00"));
        sp1.setCommissionAmount(BigDecimal.ZERO);
        sp1.setNetRevenue(new BigDecimal("630.00"));
        sp1.setOrderDate(LocalDateTime.now().minusHours(2));

        SalesProjection sp2 = new SalesProjection();
        sp2.setOrderId(101L);
        sp2.setOrderNumber("ORD-101");
        sp2.setOrderType("DELIVERY");
        sp2.setPlatform("RAPPI");
        sp2.setSubtotal(new BigDecimal("400.00"));
        sp2.setTax(new BigDecimal("64.00"));
        sp2.setTip(BigDecimal.ZERO);
        sp2.setTotal(new BigDecimal("464.00"));
        sp2.setCommissionAmount(new BigDecimal("72.00"));
        sp2.setNetRevenue(new BigDecimal("392.00"));
        sp2.setOrderDate(LocalDateTime.now().minusHours(1));

        when(salesRepository.findByOrderDateBetween(any(), any()))
                .thenReturn(List.of(sp1, sp2));

        SalesReportResponse report = reportService.getSalesReport(
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now()
        );

        assertNotNull(report);
        assertEquals(2, report.totalOrders());
        assertEquals(new BigDecimal("1094.00"), report.totalSales());
        assertEquals(new BigDecimal("900.00"), report.totalSubtotal());
        assertEquals(new BigDecimal("144.00"), report.totalTax());
        assertEquals(new BigDecimal("50.00"), report.totalTips());
        assertEquals(new BigDecimal("72.00"), report.totalCommissions());
        assertEquals(new BigDecimal("1022.00"), report.netRevenue());
        assertEquals(new BigDecimal("547.00"), report.averageTicket());

        assertEquals(new BigDecimal("630.00"), report.salesByOrderType().get("DINE_IN"));
        assertEquals(new BigDecimal("464.00"), report.salesByOrderType().get("DELIVERY"));

        assertEquals(new BigDecimal("630.00"), report.salesByPlatform().get("DIRECT"));
        assertEquals(new BigDecimal("464.00"), report.salesByPlatform().get("RAPPI"));
    }

    @Test
    void getSalesReport_WhenNoOrders_ShouldReturnZeros() {
        when(salesRepository.findByOrderDateBetween(any(), any()))
                .thenReturn(List.of());

        SalesReportResponse report = reportService.getSalesReport(null, null);

        assertNotNull(report);
        assertEquals(0, report.totalOrders());
        assertEquals(BigDecimal.ZERO, report.totalSales());
        assertEquals(BigDecimal.ZERO, report.averageTicket());
    }

    @Test
    void getInventoryReport_ShouldReturnAlertsFromClient() {
        var alert = new InventoryReportResponse.InventoryAlertDto(
                1L, "Carne Molida", "KG", new BigDecimal("3.00"), new BigDecimal("10.00"),
                new BigDecimal("7.00"), "CRITICAL"
        );
        when(inventoryClient.getLowStockAlerts(any())).thenReturn(List.of(alert));

        InventoryReportResponse response = reportService.getInventoryReport("Bearer test");

        assertNotNull(response);
        assertEquals(1, response.totalAlerts());
        assertEquals("Carne Molida", response.lowStockAlerts().get(0).name());
        verify(inventoryClient).getLowStockAlerts("Bearer test");
    }
}
