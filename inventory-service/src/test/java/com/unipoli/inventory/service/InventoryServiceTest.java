package com.unipoli.inventory.service;

import com.unipoli.inventory.dto.*;
import com.unipoli.inventory.exception.InsufficientStockException;
import com.unipoli.inventory.model.*;
import com.unipoli.inventory.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private IngredientRepository ingredientRepository;

    @Mock
    private DishRecipeRepository dishRecipeRepository;

    @Mock
    private InventoryMovementRepository movementRepository;

    @Mock
    private ProcessedOrderDeductionRepository deductionRepository;

    @InjectMocks
    private InventoryService service;

    private Ingredient tomato;

    @BeforeEach
    void setUp() {
        tomato = new Ingredient();
        tomato.setId(1L);
        tomato.setName("Tomato");
        tomato.setUnitOfMeasure("KG");
        tomato.setCurrentStock(new BigDecimal("10.000"));
        tomato.setMinimumStock(new BigDecimal("2.000"));
        tomato.setUnitCost(new BigDecimal("25.50"));
        tomato.setCreatedAt(LocalDateTime.now());
        tomato.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void createIngredient_WhenValid_ShouldSaveAndRecordInitialMovement() {
        CreateIngredientRequest request = new CreateIngredientRequest(
                "Tomato",
                "KG",
                new BigDecimal("10.000"),
                new BigDecimal("2.000"),
                new BigDecimal("25.50")
        );

        when(ingredientRepository.existsByNameIgnoreCase("Tomato")).thenReturn(false);
        when(ingredientRepository.save(any(Ingredient.class))).thenReturn(tomato);

        IngredientResponse response = service.createIngredient(request);

        assertNotNull(response);
        assertEquals("Tomato", response.name());
        verify(movementRepository, times(1)).save(any(InventoryMovement.class));
    }

    @Test
    void recordMovement_WhenInMovement_ShouldIncreaseStock() {
        RecordMovementRequest request = new RecordMovementRequest(
                1L,
                MovementType.IN,
                new BigDecimal("5.000"),
                "PO-100",
                "Supplier delivery"
        );

        when(ingredientRepository.findById(1L)).thenReturn(Optional.of(tomato));
        when(movementRepository.save(any(InventoryMovement.class))).thenAnswer(i -> {
            InventoryMovement m = i.getArgument(0);
            m.setId(10L);
            return m;
        });

        MovementResponse response = service.recordMovement(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("15.000"), tomato.getCurrentStock());
        assertEquals(new BigDecimal("10.000"), response.previousStock());
        assertEquals(new BigDecimal("15.000"), response.newStock());
    }

    @Test
    void recordMovement_WhenOutMovementWithInsufficientStock_ShouldThrowException() {
        RecordMovementRequest request = new RecordMovementRequest(
                1L,
                MovementType.OUT,
                new BigDecimal("15.000"),
                null,
                "Kitchen use"
        );

        when(ingredientRepository.findById(1L)).thenReturn(Optional.of(tomato));

        assertThrows(InsufficientStockException.class, () -> service.recordMovement(request));
    }

    @Test
    void getLowStockAlerts_ShouldReturnIngredientsAtOrBelowMinimum() {
        tomato.setCurrentStock(new BigDecimal("1.500"));
        when(ingredientRepository.findLowStockAlerts()).thenReturn(List.of(tomato));

        List<InventoryAlertResponse> alerts = service.getLowStockAlerts();

        assertEquals(1, alerts.size());
        assertEquals("Tomato", alerts.get(0).name());
        assertEquals(new BigDecimal("0.500"), alerts.get(0).deficit());
    }

    @Test
    void deductForOrder_WhenAlreadyProcessed_ShouldReturnIdempotentResponse() {
        DeductOrderInventoryRequest request = new DeductOrderInventoryRequest(
                100L,
                List.of(new DeductOrderInventoryRequest.OrderItemDeduction(1L, 2))
        );

        when(deductionRepository.existsByOrderId(100L)).thenReturn(true);

        DeductOrderInventoryResponse response = service.deductForOrder(request);

        assertTrue(response.success());
        assertTrue(response.alreadyProcessed());
        verify(ingredientRepository, never()).save(any());
    }

    @Test
    void deductForOrder_WhenValid_ShouldDeductStockAndSaveProcessed() {
        DeductOrderInventoryRequest request = new DeductOrderInventoryRequest(
                100L,
                List.of(new DeductOrderInventoryRequest.OrderItemDeduction(1L, 2))
        );

        DishRecipe recipe = new DishRecipe(1L, tomato, new BigDecimal("0.500"));

        when(deductionRepository.existsByOrderId(100L)).thenReturn(false);
        when(dishRecipeRepository.findByDishId(1L)).thenReturn(List.of(recipe));
        when(ingredientRepository.findById(1L)).thenReturn(Optional.of(tomato));

        DeductOrderInventoryResponse response = service.deductForOrder(request);

        assertTrue(response.success());
        assertFalse(response.alreadyProcessed());
        // 2 items * 0.500 = 1.000 deducted from 10.000 = 9.000
        assertEquals(new BigDecimal("9.000"), tomato.getCurrentStock());
        verify(deductionRepository, times(1)).save(any(ProcessedOrderDeduction.class));
        verify(movementRepository, times(1)).save(any(InventoryMovement.class));
    }
}
