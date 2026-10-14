package com.unipoli.inventory.repository;

import com.unipoli.inventory.model.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    Optional<Ingredient> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    @Query("SELECT i FROM Ingredient i WHERE i.currentStock <= i.minimumStock ORDER BY i.currentStock ASC")
    List<Ingredient> findLowStockAlerts();
}
