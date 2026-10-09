package com.unipoli.table.repository;

import com.unipoli.table.model.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantTableRepository
        extends JpaRepository<RestaurantTable, Long> {
}
