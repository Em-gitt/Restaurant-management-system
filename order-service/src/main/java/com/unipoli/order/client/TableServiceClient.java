package com.unipoli.order.client;

import com.unipoli.order.dto.TableInfo;
import com.unipoli.order.exception.ExternalServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class TableServiceClient {

    private final RestClient restClient;

    public TableServiceClient(@Value("${services.table.url:http://localhost:8082}") String tableServiceUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(tableServiceUrl)
                .build();
    }

    public TableInfo getTableById(Long tableId, String authHeader) {
        try {
            var requestSpec = restClient.get()
                    .uri("/api/v1/tables");

            if (authHeader != null && !authHeader.isBlank()) {
                requestSpec.header("Authorization", authHeader);
            }

            List<TableInfo> tables = requestSpec.retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (tables == null) {
                throw new ExternalServiceException("Table service returned empty list");
            }

            return tables.stream()
                    .filter(t -> t.id().equals(tableId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Table not found with id: " + tableId));

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new ExternalServiceException("Error verifying table with Table Service: " + e.getMessage());
        }
    }

    public void updateTableStatus(Long tableId, String status, String authHeader) {
        try {
            var requestSpec = restClient.put()
                    .uri("/api/v1/tables/{tableId}/status", tableId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("status", status));

            if (authHeader != null && !authHeader.isBlank()) {
                requestSpec.header("Authorization", authHeader);
            }

            requestSpec.retrieve().toBodilessEntity();
        } catch (Exception e) {
            // Log but don't fail transactional rollback if table service is unavailable in isolated flow
            System.err.println("Warning: Could not update table status: " + e.getMessage());
        }
    }
}
