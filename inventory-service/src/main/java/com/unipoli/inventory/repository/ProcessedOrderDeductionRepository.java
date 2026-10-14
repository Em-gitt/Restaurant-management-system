package com.unipoli.inventory.repository;

import com.unipoli.inventory.model.ProcessedOrderDeduction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProcessedOrderDeductionRepository extends JpaRepository<ProcessedOrderDeduction, Long> {

    boolean existsByOrderId(Long orderId);

    Optional<ProcessedOrderDeduction> findByOrderId(Long orderId);
}
