package com.unipoli.inventory.repository;

import com.unipoli.inventory.model.DishRecipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DishRecipeRepository extends JpaRepository<DishRecipe, Long> {

    List<DishRecipe> findByDishId(Long dishId);

    void deleteByDishId(Long dishId);
}
