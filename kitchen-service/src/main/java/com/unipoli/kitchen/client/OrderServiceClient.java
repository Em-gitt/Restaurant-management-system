package com.unipoli.kitchen.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public class OrderServiceClient {

    private final RestClient restClient;

    public OrderServiceClient(@Value("${services.order.url:http://localhost:8086}") String orderServiceUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(orderServiceUrl)
                .build();
    }

    public void notifyOrderReady(Long orderId, String orderNumber) {
        try {
            restClient.post()
                    .uri("/api/v1/orders/events/order-ready")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "orderId", orderId,
                            "orderNumber", orderNumber,
                            "readyAt", LocalDateTime.now().toString()
                    ))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            System.err.println("Warning: Order Service not reached when notifying OrderReady: " + e.getMessage());
        }
    }
}
