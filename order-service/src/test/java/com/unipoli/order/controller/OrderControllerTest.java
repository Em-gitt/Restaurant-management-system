package com.unipoli.order.controller;

import com.unipoli.order.dto.CreateOrderRequest;
import com.unipoli.order.dto.OrderResponse;
import com.unipoli.order.model.OrderStatus;
import com.unipoli.order.model.OrderType;
import com.unipoli.order.security.JwtAuthenticationFilter;
import com.unipoli.order.security.JwtService;
import com.unipoli.order.service.OrderService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = OrderController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {JwtAuthenticationFilter.class, JwtService.class})
)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Test
    void create_WhenValid_ShouldReturnCreated() throws Exception {
        OrderResponse response = new OrderResponse(
                1L,
                "ORD-2026-001",
                OrderType.DINE_IN,
                OrderStatus.CONFIRMED,
                1L,
                2L,
                "Pedro",
                "555-1111",
                null,
                new BigDecimal("200.00"),
                new BigDecimal("32.00"),
                new BigDecimal("20.00"),
                new BigDecimal("252.00"),
                "None",
                LocalDateTime.now(),
                LocalDateTime.now(),
                List.of(new OrderResponse.OrderItemResponse(1L, 1L, "Tacos", 2, new BigDecimal("100.00"), new BigDecimal("200.00"), null))
        );

        when(orderService.createOrder(any(CreateOrderRequest.class), any())).thenReturn(response);

        String json = """
                {
                    "orderType": "DINE_IN",
                    "tableId": 1,
                    "waiterId": 2,
                    "customerName": "Pedro",
                    "customerPhone": "555-1111",
                    "tip": 20.00,
                    "notes": "None",
                    "items": [
                        {
                            "dishId": 1,
                            "quantity": 2
                        }
                    ]
                }
                """;

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderNumber").value("ORD-2026-001"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.total").value(252.00));
    }

    @Test
    void findById_ShouldReturnOrder() throws Exception {
        OrderResponse response = new OrderResponse(
                1L,
                "ORD-2026-001",
                OrderType.DINE_IN,
                OrderStatus.CONFIRMED,
                1L,
                2L,
                "Pedro",
                "555-1111",
                null,
                new BigDecimal("200.00"),
                new BigDecimal("32.00"),
                BigDecimal.ZERO,
                new BigDecimal("232.00"),
                null,
                LocalDateTime.now(),
                LocalDateTime.now(),
                List.of()
        );

        when(orderService.findById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void cancel_ShouldReturnCancelledOrder() throws Exception {
        OrderResponse response = new OrderResponse(
                1L,
                "ORD-2026-001",
                OrderType.DINE_IN,
                OrderStatus.CANCELLED,
                1L,
                2L,
                "Pedro",
                "555-1111",
                null,
                new BigDecimal("200.00"),
                new BigDecimal("32.00"),
                BigDecimal.ZERO,
                new BigDecimal("232.00"),
                null,
                LocalDateTime.now(),
                LocalDateTime.now(),
                List.of()
        );

        when(orderService.cancelOrder(eq(1L), any())).thenReturn(response);

        mockMvc.perform(put("/api/v1/orders/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }
}
