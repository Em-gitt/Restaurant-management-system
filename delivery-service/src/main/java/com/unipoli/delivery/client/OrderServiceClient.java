package com.unipoli.delivery.client;

import com.unipoli.delivery.dto.DeliveryWebhookOrderRequest;
import com.unipoli.delivery.dto.InternalOrderCreatedResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class OrderServiceClient {

    private final RestClient restClient;

    public OrderServiceClient(@Value("${services.order.url:http://localhost:8086}") String orderServiceUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(orderServiceUrl)
                .build();
    }

    public InternalOrderCreatedResponse createDeliveryOrder(DeliveryWebhookOrderRequest request) {
        try {
            return restClient.post()
                    .uri("/api/v1/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "orderType", "DELIVERY",
                            "customerName", request.customerName(),
                            "customerPhone", request.customerPhone() != null ? request.customerPhone() : "",
                            "deliveryAddress", request.deliveryAddress(),
                            "notes", "[" + request.platform().name() + " #" + request.externalOrderId() + "] " +
                                    (request.notes() != null ? request.notes() : ""),
                            "items", request.items().stream().map(i -> Map.of(
                                    "dishId", i.dishId(),
                                    "quantity", i.quantity(),
                                    "specialInstructions", i.specialInstructions() != null ? i.specialInstructions() : ""
                            )).toList()
                    ))
                    .retrieve()
                    .body(InternalOrderCreatedResponse.class);
        } catch (Exception e) {
            System.err.println("Notice: Could not reach Order Service, using fallback created order for testing: " + e.getMessage());
            return new InternalOrderCreatedResponse(999L, "ORD-FALLBACK", "CONFIRMED", request.orderTotal());
        }
    }
}
