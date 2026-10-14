package com.unipoli.report.controller;

import com.unipoli.report.dto.InventoryReportResponse;
import com.unipoli.report.dto.SalesReportResponse;
import com.unipoli.report.dto.SyncOrderSalesRequest;
import com.unipoli.report.model.SalesProjection;
import com.unipoli.report.security.JwtAuthenticationFilter;
import com.unipoli.report.security.JwtService;
import com.unipoli.report.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ReportController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {JwtAuthenticationFilter.class, JwtService.class})
)
@AutoConfigureMockMvc(addFilters = false)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReportService reportService;

    @Test
    void syncOrderSales_WhenValid_ShouldReturnCreated() throws Exception {
        SalesProjection projection = new SalesProjection();
        projection.setId(1L);
        projection.setOrderId(100L);
        projection.setOrderNumber("ORD-100");
        projection.setOrderType("DINE_IN");
        projection.setPlatform("DIRECT");
        projection.setOrderStatus("PAID");
        projection.setTotal(new BigDecimal("630.00"));
        projection.setCommissionAmount(BigDecimal.ZERO);
        projection.setNetRevenue(new BigDecimal("630.00"));
        projection.setOrderDate(LocalDateTime.now());

        when(reportService.syncOrderSales(any(SyncOrderSalesRequest.class))).thenReturn(projection);

        String json = """
                {
                    "orderId": 100,
                    "orderNumber": "ORD-100",
                    "orderType": "DINE_IN",
                    "platform": "DIRECT",
                    "orderStatus": "PAID",
                    "subtotal": 500.00,
                    "tax": 80.00,
                    "tip": 50.00,
                    "total": 630.00,
                    "commissionAmount": 0.00
                }
                """;

        mockMvc.perform(post("/api/v1/reports/sync/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(100))
                .andExpect(jsonPath("$.orderNumber").value("ORD-100"))
                .andExpect(jsonPath("$.total").value(630.00));
    }

    @Test
    void getSalesReport_ShouldReturnReport() throws Exception {
        SalesReportResponse response = new SalesReportResponse(
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now(),
                5L,
                new BigDecimal("2500.00"),
                new BigDecimal("2100.00"),
                new BigDecimal("336.00"),
                new BigDecimal("100.00"),
                new BigDecimal("72.00"),
                new BigDecimal("2428.00"),
                new BigDecimal("500.00"),
                Map.of("DINE_IN", new BigDecimal("2000.00"), "DELIVERY", new BigDecimal("500.00")),
                Map.of("DIRECT", new BigDecimal("2000.00"), "RAPPI", new BigDecimal("500.00"))
        );

        when(reportService.getSalesReport(any(), any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/reports/sales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalOrders").value(5))
                .andExpect(jsonPath("$.totalSales").value(2500.00))
                .andExpect(jsonPath("$.averageTicket").value(500.00))
                .andExpect(jsonPath("$.salesByOrderType.DINE_IN").value(2000.00));
    }

    @Test
    void getInventoryReport_ShouldReturnReport() throws Exception {
        InventoryReportResponse response = new InventoryReportResponse(0, Collections.emptyList());
        when(reportService.getInventoryReport(any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/reports/inventory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAlerts").value(0))
                .andExpect(jsonPath("$.lowStockAlerts").isArray());
    }

    @Test
    void getAllSalesProjections_ShouldReturnList() throws Exception {
        SalesProjection projection = new SalesProjection();
        projection.setId(1L);
        projection.setOrderId(100L);
        projection.setOrderNumber("ORD-100");
        projection.setOrderType("DINE_IN");
        projection.setPlatform("DIRECT");
        projection.setOrderStatus("PAID");
        projection.setTotal(new BigDecimal("630.00"));

        when(reportService.getAllProjections()).thenReturn(List.of(projection));

        mockMvc.perform(get("/api/v1/reports/sales/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderNumber").value("ORD-100"));
    }
}
