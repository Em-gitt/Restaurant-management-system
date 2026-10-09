package com.unipoli.menu.repository;

import com.unipoli.menu.model.Dish;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DishRepository extends JpaRepository<Dish, Long> {
}

