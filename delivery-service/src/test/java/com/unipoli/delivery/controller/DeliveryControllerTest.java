package com.unipoli.delivery.controller;

import com.unipoli.delivery.dto.DeliveryOrderResponse;
import com.unipoli.delivery.dto.DeliveryWebhookOrderRequest;
import com.unipoli.delivery.model.DeliveryPlatform;
import com.unipoli.delivery.model.DeliveryStatus;
import com.unipoli.delivery.security.JwtAuthenticationFilter;
import com.unipoli.delivery.security.JwtService;
import com.unipoli.delivery.service.DeliveryService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = DeliveryController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {JwtAuthenticationFilter.class, JwtService.class})
)
@AutoConfigureMockMvc(addFilters = false)
class DeliveryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DeliveryService deliveryService;

    @Test
    void handleWebhookOrder_WhenValid_ShouldReturnCreated() throws Exception {
        DeliveryOrderResponse response = new DeliveryOrderResponse(
                1L,
                DeliveryPlatform.RAPPI,
                "RAPPI-001",
                10L,
                "Carlos",
                "555-1212",
                "Reforma 222",
                new BigDecimal("400.00"),
                new BigDecimal("18.00"),
                new BigDecimal("72.00"),
                new BigDecimal("328.00"),
                DeliveryStatus.ORDER_CREATED,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(deliveryService.processWebhookOrder(any(DeliveryWebhookOrderRequest.class), eq("test-secret")))
                .thenReturn(response);

        String json = """
                {
                    "platform": "RAPPI",
                    "externalOrderId": "RAPPI-001",
                    "customerName": "Carlos",
                    "customerPhone": "555-1212",
                    "deliveryAddress": "Reforma 222",
                    "orderTotal": 400.00,
                    "items": [
                        {
                            "dishId": 1,
                            "quantity": 2
                        }
                    ]
                }
                """;

        mockMvc.perform(post("/api/v1/delivery/webhooks/orders")
                        .header("X-Delivery-Webhook-Secret", "test-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.platform").value("RAPPI"))
                .andExpect(jsonPath("$.externalOrderId").value("RAPPI-001"))
                .andExpect(jsonPath("$.netPayout").value(328.00));
    }

    @Test
    void findById_ShouldReturnDeliveryOrder() throws Exception {
        DeliveryOrderResponse response = new DeliveryOrderResponse(
                1L,
                DeliveryPlatform.DIDI_FOOD,
                "DIDI-001",
                11L,
                "Ana",
                "555-3434",
                "Insurgentes 500",
                new BigDecimal("300.00"),
                new BigDecimal("15.00"),
                new BigDecimal("45.00"),
                new BigDecimal("255.00"),
                DeliveryStatus.DELIVERED,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(deliveryService.findById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/delivery/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("DELIVERED"));
    }
}
