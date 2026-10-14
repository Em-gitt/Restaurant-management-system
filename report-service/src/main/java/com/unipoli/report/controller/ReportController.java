package com.unipoli.report.controller;

import com.unipoli.report.dto.InventoryReportResponse;
import com.unipoli.report.dto.SalesReportResponse;
import com.unipoli.report.dto.SyncOrderSalesRequest;
import com.unipoli.report.model.SalesProjection;
import com.unipoli.report.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/sync/order")
    public ResponseEntity<SalesProjection> syncOrderSales(@Valid @RequestBody SyncOrderSalesRequest request) {
        SalesProjection projection = reportService.syncOrderSales(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(projection);
    }

    @GetMapping("/sales")
    public ResponseEntity<SalesReportResponse> getSalesReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        SalesReportResponse response = reportService.getSalesReport(startDate, endDate);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/inventory")
    public ResponseEntity<InventoryReportResponse> getInventoryReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        InventoryReportResponse response = reportService.getInventoryReport(authHeader);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sales/orders")
    public ResponseEntity<List<SalesProjection>> getAllSalesProjections() {
        return ResponseEntity.ok(reportService.getAllProjections());
    }
}
