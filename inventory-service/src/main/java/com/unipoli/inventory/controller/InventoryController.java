package com.unipoli.inventory.controller;

import com.unipoli.inventory.dto.*;
import com.unipoli.inventory.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/ingredients")
    public ResponseEntity<IngredientResponse> createIngredient(@Valid @RequestBody CreateIngredientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.createIngredient(request));
    }

    @GetMapping("/ingredients")
    public ResponseEntity<List<IngredientResponse>> findAllIngredients() {
        return ResponseEntity.ok(inventoryService.findAllIngredients());
    }

    @GetMapping("/ingredients/{id}")
    public ResponseEntity<IngredientResponse> findIngredientById(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.findIngredientById(id));
    }

    @PutMapping("/ingredients/{id}")
    public ResponseEntity<IngredientResponse> updateIngredient(
            @PathVariable Long id,
            @Valid @RequestBody UpdateIngredientRequest request
    ) {
        return ResponseEntity.ok(inventoryService.updateIngredient(id, request));
    }

    @PostMapping("/movements")
    public ResponseEntity<MovementResponse> recordMovement(@Valid @RequestBody RecordMovementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.recordMovement(request));
    }

    @GetMapping("/movements")
    public ResponseEntity<List<MovementResponse>> findAllMovements() {
        return ResponseEntity.ok(inventoryService.findAllMovements());
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<InventoryAlertResponse>> getLowStockAlerts() {
        return ResponseEntity.ok(inventoryService.getLowStockAlerts());
    }

    @PostMapping("/recipes/{dishId}")
    public ResponseEntity<DishRecipeResponse> saveRecipe(
            @PathVariable Long dishId,
            @Valid @RequestBody List<RecipeItemRequest> items
    ) {
        return ResponseEntity.ok(inventoryService.saveRecipe(dishId, items));
    }

    @GetMapping("/recipes/{dishId}")
    public ResponseEntity<DishRecipeResponse> getRecipe(@PathVariable Long dishId) {
        return ResponseEntity.ok(inventoryService.getRecipe(dishId));
    }

    @PostMapping("/deduct-for-order")
    public ResponseEntity<DeductOrderInventoryResponse> deductForOrder(
            @Valid @RequestBody DeductOrderInventoryRequest request
    ) {
        return ResponseEntity.ok(inventoryService.deductForOrder(request));
    }
}
