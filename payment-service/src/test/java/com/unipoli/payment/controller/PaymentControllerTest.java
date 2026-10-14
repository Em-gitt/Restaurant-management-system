package com.unipoli.payment.controller;

import com.unipoli.payment.dto.PaymentResponse;
import com.unipoli.payment.dto.ProcessPaymentRequest;
import com.unipoli.payment.model.PaymentMethod;
import com.unipoli.payment.model.PaymentStatus;
import com.unipoli.payment.security.JwtAuthenticationFilter;
import com.unipoli.payment.security.JwtService;
import com.unipoli.payment.service.PaymentService;
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
        controllers = PaymentController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {JwtAuthenticationFilter.class, JwtService.class})
)
@AutoConfigureMockMvc(addFilters = false)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @Test
    void processPayment_WhenValid_ShouldReturnCreated() throws Exception {
        PaymentResponse response = new PaymentResponse(
                1L,
                100L,
                "TXN-2026-001",
                PaymentMethod.CASH,
                PaymentStatus.APPROVED,
                new BigDecimal("200.00"),
                new BigDecimal("20.00"),
                new BigDecimal("220.00"),
                new BigDecimal("250.00"),
                new BigDecimal("30.00"),
                "MXN",
                null,
                null,
                null,
                "REC-001",
                "Cash",
                LocalDateTime.now()
        );

        when(paymentService.processPayment(any(ProcessPaymentRequest.class), any())).thenReturn(response);

        String json = """
                {
                    "orderId": 100,
                    "paymentMethod": "CASH",
                    "amount": 200.00,
                    "tip": 20.00,
                    "cashTendered": 250.00,
                    "notes": "Cash"
                }
                """;

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionNumber").value("TXN-2026-001"))
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.changeGiven").value(30.00));
    }

    @Test
    void findById_ShouldReturnPayment() throws Exception {
        PaymentResponse response = new PaymentResponse(
                1L,
                100L,
                "TXN-2026-001",
                PaymentMethod.CASH,
                PaymentStatus.APPROVED,
                new BigDecimal("200.00"),
                BigDecimal.ZERO,
                new BigDecimal("200.00"),
                new BigDecimal("200.00"),
                BigDecimal.ZERO,
                "MXN",
                null,
                null,
                null,
                "REC-001",
                null,
                LocalDateTime.now()
        );

        when(paymentService.findById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/payments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void refund_ShouldReturnRefundedPayment() throws Exception {
        PaymentResponse response = new PaymentResponse(
                1L,
                100L,
                "TXN-2026-001",
                PaymentMethod.CASH,
                PaymentStatus.REFUNDED,
                new BigDecimal("200.00"),
                BigDecimal.ZERO,
                new BigDecimal("200.00"),
                new BigDecimal("200.00"),
                BigDecimal.ZERO,
                "MXN",
                null,
                null,
                null,
                "REC-001",
                null,
                LocalDateTime.now()
        );

        when(paymentService.refundPayment(eq(1L))).thenReturn(response);

        mockMvc.perform(post("/api/v1/payments/1/refund"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REFUNDED"));
    }
}
