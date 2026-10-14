package com.unipoli.inventory.service;

import com.unipoli.inventory.dto.*;
import com.unipoli.inventory.exception.InsufficientStockException;
import com.unipoli.inventory.exception.ResourceNotFoundException;
import com.unipoli.inventory.model.*;
import com.unipoli.inventory.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@Transactional
public class InventoryService {

    private final IngredientRepository ingredientRepository;
    private final DishRecipeRepository dishRecipeRepository;
    private final InventoryMovementRepository movementRepository;
    private final ProcessedOrderDeductionRepository deductionRepository;

    public InventoryService(
            IngredientRepository ingredientRepository,
            DishRecipeRepository dishRecipeRepository,
            InventoryMovementRepository movementRepository,
            ProcessedOrderDeductionRepository deductionRepository
    ) {
        this.ingredientRepository = ingredientRepository;
        this.dishRecipeRepository = dishRecipeRepository;
        this.movementRepository = movementRepository;
        this.deductionRepository = deductionRepository;
    }

    public IngredientResponse createIngredient(CreateIngredientRequest request) {
        if (ingredientRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw new IllegalArgumentException("Ingredient '" + request.name() + "' already exists");
        }

        Ingredient ingredient = new Ingredient();
        ingredient.setName(request.name().trim());
        ingredient.setUnitOfMeasure(request.unitOfMeasure().trim().toUpperCase());
        ingredient.setCurrentStock(request.initialStock());
        ingredient.setMinimumStock(request.minimumStock());
        ingredient.setUnitCost(request.unitCost());

        Ingredient saved = ingredientRepository.save(ingredient);

        if (request.initialStock().compareTo(BigDecimal.ZERO) > 0) {
            InventoryMovement movement = new InventoryMovement();
            movement.setIngredient(saved);
            movement.setMovementType(MovementType.IN);
            movement.setQuantity(request.initialStock());
            movement.setPreviousStock(BigDecimal.ZERO);
            movement.setNewStock(request.initialStock());
            movement.setReason("Initial inventory registration");
            movementRepository.save(movement);
        }

        return toIngredientResponse(saved);
    }

    public IngredientResponse updateIngredient(Long id, UpdateIngredientRequest request) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ingredient not found with id: " + id));

        if (request.name() != null && !request.name().isBlank()) {
            ingredient.setName(request.name().trim());
        }
        if (request.unitOfMeasure() != null && !request.unitOfMeasure().isBlank()) {
            ingredient.setUnitOfMeasure(request.unitOfMeasure().trim().toUpperCase());
        }
        if (request.minimumStock() != null) {
            ingredient.setMinimumStock(request.minimumStock());
        }
        if (request.unitCost() != null) {
            ingredient.setUnitCost(request.unitCost());
        }

        Ingredient updated = ingredientRepository.save(ingredient);
        return toIngredientResponse(updated);
    }

    @Transactional(readOnly = true)
    public List<IngredientResponse> findAllIngredients() {
        return ingredientRepository.findAll().stream()
                .map(this::toIngredientResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public IngredientResponse findIngredientById(Long id) {
        return ingredientRepository.findById(id)
                .map(this::toIngredientResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Ingredient not found with id: " + id));
    }

    public MovementResponse recordMovement(RecordMovementRequest request) {
        Ingredient ingredient = ingredientRepository.findById(request.ingredientId())
                .orElseThrow(() -> new ResourceNotFoundException("Ingredient not found with id: " + request.ingredientId()));

        BigDecimal previousStock = ingredient.getCurrentStock();
        BigDecimal newStock;

        switch (request.movementType()) {
            case IN -> newStock = previousStock.add(request.quantity());
            case OUT, ORDER_CONSUMPTION -> {
                if (previousStock.compareTo(request.quantity()) < 0) {
                    throw new InsufficientStockException(
                            "Insufficient stock for ingredient '" + ingredient.getName() +
                                    "'. Available: " + previousStock + " " + ingredient.getUnitOfMeasure() +
                                    ", requested: " + request.quantity()
                    );
                }
                newStock = previousStock.subtract(request.quantity());
            }
            case ADJUSTMENT -> newStock = request.quantity();
            default -> throw new IllegalArgumentException("Unknown movement type: " + request.movementType());
        }

        ingredient.setCurrentStock(newStock);
        ingredientRepository.save(ingredient);

        InventoryMovement movement = new InventoryMovement();
        movement.setIngredient(ingredient);
        movement.setMovementType(request.movementType());
        movement.setQuantity(request.quantity());
        movement.setPreviousStock(previousStock);
        movement.setNewStock(newStock);
        movement.setReferenceId(request.referenceId());
        movement.setReason(request.reason());

        InventoryMovement saved = movementRepository.save(movement);
        return toMovementResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<MovementResponse> findAllMovements() {
        return movementRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toMovementResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InventoryAlertResponse> getLowStockAlerts() {
        return ingredientRepository.findLowStockAlerts().stream()
                .map(i -> {
                    BigDecimal deficit = i.getMinimumStock().subtract(i.getCurrentStock());
                    String alertLevel = i.getCurrentStock().compareTo(BigDecimal.ZERO) <= 0 ? "CRITICAL" : "WARNING";
                    return new InventoryAlertResponse(
                            i.getId(),
                            i.getName(),
                            i.getUnitOfMeasure(),
                            i.getCurrentStock(),
                            i.getMinimumStock(),
                            deficit.max(BigDecimal.ZERO),
                            alertLevel
                    );
                })
                .toList();
    }

    public DishRecipeResponse saveRecipe(Long dishId, List<RecipeItemRequest> items) {
        dishRecipeRepository.deleteByDishId(dishId);

        List<DishRecipe> recipes = new ArrayList<>();
        for (RecipeItemRequest item : items) {
            Ingredient ingredient = ingredientRepository.findById(item.ingredientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ingredient not found with id: " + item.ingredientId()));

            DishRecipe recipe = new DishRecipe(dishId, ingredient, item.quantityRequired());
            recipes.add(dishRecipeRepository.save(recipe));
        }

        return toDishRecipeResponse(dishId, recipes);
    }

    @Transactional(readOnly = true)
    public DishRecipeResponse getRecipe(Long dishId) {
        List<DishRecipe> recipes = dishRecipeRepository.findByDishId(dishId);
        return toDishRecipeResponse(dishId, recipes);
    }

    public DeductOrderInventoryResponse deductForOrder(DeductOrderInventoryRequest request) {
        if (deductionRepository.existsByOrderId(request.orderId())) {
            return new DeductOrderInventoryResponse(
                    request.orderId(),
                    true,
                    true,
                    "Order inventory was already deducted previously",
                    Collections.emptyList()
            );
        }

        // Aggregate required quantity per ingredient across all ordered dishes
        Map<Long, BigDecimal> aggregatedRequirements = new HashMap<>();

        for (DeductOrderInventoryRequest.OrderItemDeduction item : request.items()) {
            List<DishRecipe> recipes = dishRecipeRepository.findByDishId(item.dishId());
            for (DishRecipe recipe : recipes) {
                Long ingredientId = recipe.getIngredient().getId();
                BigDecimal totalNeededForDish = recipe.getQuantityRequired()
                        .multiply(BigDecimal.valueOf(item.quantity()));

                aggregatedRequirements.merge(ingredientId, totalNeededForDish, BigDecimal::add);
            }
        }

        // If no recipes are defined for the dishes, mark processed and return
        if (aggregatedRequirements.isEmpty()) {
            deductionRepository.save(new ProcessedOrderDeduction(request.orderId()));
            return new DeductOrderInventoryResponse(
                    request.orderId(),
                    true,
                    false,
                    "No recipe ingredients associated with this order's dishes",
                    Collections.emptyList()
            );
        }

        // Validate that all ingredients exist and have sufficient stock
        Map<Long, Ingredient> ingredientEntities = new HashMap<>();
        for (Map.Entry<Long, BigDecimal> entry : aggregatedRequirements.entrySet()) {
            Ingredient ingredient = ingredientRepository.findById(entry.getKey())
                    .orElseThrow(() -> new ResourceNotFoundException("Ingredient not found with id: " + entry.getKey()));

            if (ingredient.getCurrentStock().compareTo(entry.getValue()) < 0) {
                throw new InsufficientStockException(
                        "Cannot fulfill order " + request.orderId() + ": insufficient stock for ingredient '" +
                                ingredient.getName() + "'. Required: " + entry.getValue() +
                                " " + ingredient.getUnitOfMeasure() + ", Available: " + ingredient.getCurrentStock()
                );
            }
            ingredientEntities.put(entry.getKey(), ingredient);
        }

        // Deduct inventory and record movements
        List<DeductOrderInventoryResponse.DeductionItemResult> results = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> entry : aggregatedRequirements.entrySet()) {
            Ingredient ingredient = ingredientEntities.get(entry.getKey());
            BigDecimal prevStock = ingredient.getCurrentStock();
            BigDecimal needed = entry.getValue();
            BigDecimal newStock = prevStock.subtract(needed);

            ingredient.setCurrentStock(newStock);
            ingredientRepository.save(ingredient);

            InventoryMovement movement = new InventoryMovement();
            movement.setIngredient(ingredient);
            movement.setMovementType(MovementType.ORDER_CONSUMPTION);
            movement.setQuantity(needed);
            movement.setPreviousStock(prevStock);
            movement.setNewStock(newStock);
            movement.setReferenceId(String.valueOf(request.orderId()));
            movement.setReason("Automatic consumption for order #" + request.orderId());
            movementRepository.save(movement);

            results.add(new DeductOrderInventoryResponse.DeductionItemResult(
                    ingredient.getId(),
                    ingredient.getName(),
                    needed,
                    newStock
            ));
        }

        deductionRepository.save(new ProcessedOrderDeduction(request.orderId()));

        return new DeductOrderInventoryResponse(
                request.orderId(),
                true,
                false,
                "Order inventory deducted successfully",
                results
        );
    }

    private IngredientResponse toIngredientResponse(Ingredient ingredient) {
        return new IngredientResponse(
                ingredient.getId(),
                ingredient.getName(),
                ingredient.getUnitOfMeasure(),
                ingredient.getCurrentStock(),
                ingredient.getMinimumStock(),
                ingredient.getUnitCost(),
                ingredient.getCreatedAt(),
                ingredient.getUpdatedAt()
        );
    }

    private MovementResponse toMovementResponse(InventoryMovement m) {
        return new MovementResponse(
                m.getId(),
                m.getIngredient().getId(),
                m.getIngredient().getName(),
                m.getMovementType(),
                m.getQuantity(),
                m.getPreviousStock(),
                m.getNewStock(),
                m.getReferenceId(),
                m.getReason(),
                m.getCreatedAt()
        );
    }

    private DishRecipeResponse toDishRecipeResponse(Long dishId, List<DishRecipe> recipes) {
        List<DishRecipeResponse.RecipeItemResponse> items = recipes.stream()
                .map(r -> new DishRecipeResponse.RecipeItemResponse(
                        r.getId(),
                        r.getIngredient().getId(),
                        r.getIngredient().getName(),
                        r.getIngredient().getUnitOfMeasure(),
                        r.getQuantityRequired()
                ))
                .toList();

        return new DishRecipeResponse(dishId, items);
    }
}
