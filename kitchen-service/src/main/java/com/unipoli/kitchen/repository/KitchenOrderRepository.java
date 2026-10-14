package com.unipoli.kitchen.repository;

import com.unipoli.kitchen.model.KitchenOrder;
import com.unipoli.kitchen.model.KitchenStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KitchenOrderRepository extends JpaRepository<KitchenOrder, Long> {

    Optional<KitchenOrder> findByOrderId(Long orderId);

    boolean existsByOrderId(Long orderId);

    List<KitchenOrder> findByStatus(KitchenStatus status);

    List<KitchenOrder> findByStatusInOrderByCreatedAtAsc(List<KitchenStatus> statuses);

    List<KitchenOrder> findAllByOrderByCreatedAtDesc();
}
