package com.unipoli.inventory.repository;

import com.unipoli.inventory.model.InventoryMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {

    List<InventoryMovement> findAllByOrderByCreatedAtDesc();

    List<InventoryMovement> findByIngredientIdOrderByCreatedAtDesc(Long ingredientId);

    List<InventoryMovement> findByReferenceId(String referenceId);
}
