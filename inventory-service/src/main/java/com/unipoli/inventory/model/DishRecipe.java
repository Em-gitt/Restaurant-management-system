package com.unipoli.inventory.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "dish_recipes", uniqueConstraints = {
        @UniqueConstraint(name = "uk_dish_recipes_dish_ingredient", columnNames = {"dish_id", "ingredient_id"})
})
public class DishRecipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dish_id", nullable = false)
    private Long dishId;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @Column(name = "quantity_required", nullable = false, precision = 12, scale = 3)
    private BigDecimal quantityRequired;

    public DishRecipe() {
    }

    public DishRecipe(Long dishId, Ingredient ingredient, BigDecimal quantityRequired) {
        this.dishId = dishId;
        this.ingredient = ingredient;
        this.quantityRequired = quantityRequired;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDishId() {
        return dishId;
    }

    public void setDishId(Long dishId) {
        this.dishId = dishId;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public void setIngredient(Ingredient ingredient) {
        this.ingredient = ingredient;
    }

    public BigDecimal getQuantityRequired() {
        return quantityRequired;
    }

    public void setQuantityRequired(BigDecimal quantityRequired) {
        this.quantityRequired = quantityRequired;
    }
}
