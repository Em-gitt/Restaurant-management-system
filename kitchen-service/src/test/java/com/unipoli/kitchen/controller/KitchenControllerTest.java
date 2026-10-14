package com.unipoli.kitchen.controller;

import com.unipoli.kitchen.dto.CreateKitchenOrderRequest;
import com.unipoli.kitchen.dto.KitchenOrderResponse;
import com.unipoli.kitchen.model.KitchenStatus;
import com.unipoli.kitchen.security.JwtAuthenticationFilter;
import com.unipoli.kitchen.security.JwtService;
import com.unipoli.kitchen.service.KitchenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = KitchenController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {JwtAuthenticationFilter.class, JwtService.class})
)
@AutoConfigureMockMvc(addFilters = false)
class KitchenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private KitchenService kitchenService;

    @Test
    void receiveOrder_WhenValid_ShouldReturnCreated() throws Exception {
        KitchenOrderResponse response = new KitchenOrderResponse(
                1L,
                100L,
                "ORD-2026-100",
                3L,
                "DINE_IN",
                KitchenStatus.PENDING,
                "No salt",
                null,
                null,
                null,
                LocalDateTime.now(),
                LocalDateTime.now(),
                List.of(new KitchenOrderResponse.KitchenItemResponse(1L, 1L, "Pozole", 1, "Sin cebolla"))
        );

        when(kitchenService.receiveOrder(any(CreateKitchenOrderRequest.class))).thenReturn(response);

        String json = """
                {
                    "orderId": 100,
                    "orderNumber": "ORD-2026-100",
                    "tableId": 3,
                    "orderType": "DINE_IN",
                    "notes": "No salt",
                    "items": [
                        {
                            "dishId": 1,
                            "dishName": "Pozole",
                            "quantity": 1,
                            "specialInstructions": "Sin cebolla"
                        }
                    ]
                }
                """;

        mockMvc.perform(post("/api/v1/kitchen/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderNumber").value("ORD-2026-100"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void findOrders_ShouldReturnList() throws Exception {
        KitchenOrderResponse response = new KitchenOrderResponse(
                1L,
                100L,
                "ORD-2026-100",
                3L,
                "DINE_IN",
                KitchenStatus.PENDING,
                "No salt",
                null,
                null,
                null,
                LocalDateTime.now(),
                LocalDateTime.now(),
                List.of()
        );

        when(kitchenService.findOrders(null)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/kitchen/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderNumber").value("ORD-2026-100"));
    }

    @Test
    void markAsReady_ShouldReturnReadyOrder() throws Exception {
        KitchenOrderResponse response = new KitchenOrderResponse(
                1L,
                100L,
                "ORD-2026-100",
                3L,
                "DINE_IN",
                KitchenStatus.READY,
                "No salt",
                LocalDateTime.now().minusMinutes(12),
                LocalDateTime.now(),
                12,
                LocalDateTime.now().minusMinutes(12),
                LocalDateTime.now(),
                List.of()
        );

        when(kitchenService.markAsReady(eq(100L))).thenReturn(response);

        mockMvc.perform(put("/api/v1/kitchen/orders/100/ready"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("READY"))
                .andExpect(jsonPath("$.preparationTimeMinutes").value(12));
    }
}
