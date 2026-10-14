package com.unipoli.report.client;

import com.unipoli.report.dto.InventoryReportResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;

@Component
public class InventoryServiceClient {

    private final RestClient restClient;

    public InventoryServiceClient(@Value("${services.inventory.url:http://localhost:8085}") String inventoryServiceUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(inventoryServiceUrl)
                .build();
    }

    public List<InventoryReportResponse.InventoryAlertDto> getLowStockAlerts(String authHeader) {
        try {
            var requestSpec = restClient.get()
                    .uri("/api/v1/inventory/alerts");

            if (authHeader != null && !authHeader.isBlank()) {
                requestSpec.header("Authorization", authHeader);
            }

            List<InventoryReportResponse.InventoryAlertDto> alerts = requestSpec.retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            return alerts != null ? alerts : Collections.emptyList();
        } catch (Exception e) {
            System.err.println("Warning: Could not fetch inventory alerts from Inventory Service: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}
