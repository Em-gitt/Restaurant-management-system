package com.unipoli.inventory.controller;

import com.unipoli.inventory.dto.*;
import com.unipoli.inventory.model.MovementType;
import com.unipoli.inventory.security.JwtAuthenticationFilter;
import com.unipoli.inventory.security.JwtService;
import com.unipoli.inventory.service.InventoryService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = InventoryController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {JwtAuthenticationFilter.class, JwtService.class})
)
@AutoConfigureMockMvc(addFilters = false)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InventoryService inventoryService;

    @Test
    void createIngredient_WhenValid_ShouldReturnCreated() throws Exception {
        IngredientResponse response = new IngredientResponse(
                1L,
                "Onion",
                "KG",
                new BigDecimal("20.000"),
                new BigDecimal("5.000"),
                new BigDecimal("18.00"),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(inventoryService.createIngredient(any(CreateIngredientRequest.class))).thenReturn(response);

        String json = """
                {
                    "name": "Onion",
                    "unitOfMeasure": "KG",
                    "initialStock": 20.000,
                    "minimumStock": 5.000,
                    "unitCost": 18.00
                }
                """;

        mockMvc.perform(post("/api/v1/inventory/ingredients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Onion"))
                .andExpect(jsonPath("$.unitOfMeasure").value("KG"));
    }

    @Test
    void getLowStockAlerts_ShouldReturnAlertList() throws Exception {
        InventoryAlertResponse alert = new InventoryAlertResponse(
                1L,
                "Tomato",
                "KG",
                new BigDecimal("1.000"),
                new BigDecimal("5.000"),
                new BigDecimal("4.000"),
                "WARNING"
        );

        when(inventoryService.getLowStockAlerts()).thenReturn(List.of(alert));

        mockMvc.perform(get("/api/v1/inventory/alerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Tomato"))
                .andExpect(jsonPath("$[0].alertLevel").value("WARNING"));
    }

    @Test
    void recordMovement_WhenValid_ShouldReturnCreated() throws Exception {
        MovementResponse response = new MovementResponse(
                1L,
                1L,
                "Tomato",
                MovementType.IN,
                new BigDecimal("10.000"),
                new BigDecimal("5.000"),
                new BigDecimal("15.000"),
                "PO-001",
                "Delivery",
                LocalDateTime.now()
        );

        when(inventoryService.recordMovement(any(RecordMovementRequest.class))).thenReturn(response);

        String json = """
                {
                    "ingredientId": 1,
                    "movementType": "IN",
                    "quantity": 10.000,
                    "referenceId": "PO-001",
                    "reason": "Delivery"
                }
                """;

        mockMvc.perform(post("/api/v1/inventory/movements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.movementType").value("IN"));
    }
}
