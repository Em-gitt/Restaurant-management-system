package com.unipoli.order.client;

import com.unipoli.order.dto.OrderEvents;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class OrderEventPublisher {

    private final RestClient kitchenClient;
    private final RestClient inventoryClient;
    private final RestClient reportClient;

    public OrderEventPublisher(
            @Value("${services.kitchen.url:http://localhost:8087}") String kitchenUrl,
            @Value("${services.inventory.url:http://localhost:8085}") String inventoryUrl,
            @Value("${services.report.url:http://localhost:8090}") String reportUrl
    ) {
        this.kitchenClient = RestClient.builder().baseUrl(kitchenUrl).build();
        this.inventoryClient = RestClient.builder().baseUrl(inventoryUrl).build();
        this.reportClient = RestClient.builder().baseUrl(reportUrl).build();
    }

    public void publishOrderCreated(OrderEvents.OrderCreatedEvent event) {
        // 1. Notify Kitchen Service to generate kitchen order
        try {
            kitchenClient.post()
                    .uri("/api/v1/kitchen/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "orderId", event.orderId(),
                            "orderNumber", event.orderNumber(),
                            "tableId", event.tableId() != null ? event.tableId() : 0,
                            "orderType", event.orderType().name(),
                            "notes", event.notes() != null ? event.notes() : "",
                            "items", event.items().stream().map(i -> Map.of(
                                    "dishId", i.dishId(),
                                    "dishName", i.dishName(),
                                    "quantity", i.quantity(),
                                    "specialInstructions", i.specialInstructions() != null ? i.specialInstructions() : ""
                            )).toList()
                    ))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            System.err.println("Warning: Kitchen Service not reached for OrderCreated event: " + e.getMessage());
        }

        // 2. Notify Inventory Service to deduct ingredients
        try {
            inventoryClient.post()
                    .uri("/api/v1/inventory/deduct-for-order")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "orderId", event.orderId(),
                            "items", event.items().stream().map(i -> Map.of(
                                    "dishId", i.dishId(),
                                    "quantity", i.quantity()
                            )).toList()
                    ))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            System.err.println("Warning: Inventory Service not reached for OrderCreated event: " + e.getMessage());
        }
    }

    public void publishOrderPaid(Long orderId, String orderNumber, String orderType, java.math.BigDecimal subtotal, java.math.BigDecimal tax, java.math.BigDecimal tip, java.math.BigDecimal total) {
        try {
            Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("orderId", orderId);
            payload.put("orderNumber", orderNumber);
            payload.put("orderType", orderType);
            payload.put("platform", "DIRECT");
            payload.put("orderStatus", "PAID");
            payload.put("subtotal", subtotal);
            payload.put("tax", tax);
            payload.put("tip", tip);
            payload.put("total", total);
            payload.put("commissionAmount", java.math.BigDecimal.ZERO);
            payload.put("orderDate", java.time.LocalDateTime.now().toString());

            reportClient.post()
                    .uri("/api/v1/reports/sync/order")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            System.err.println("Warning: Report Service not reached for OrderPaid event: " + e.getMessage());
        }
    }
}
