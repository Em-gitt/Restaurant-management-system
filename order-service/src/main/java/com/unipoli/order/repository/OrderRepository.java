package com.unipoli.order.repository;

import com.unipoli.order.model.Order;
import com.unipoli.order.model.OrderStatus;
import com.unipoli.order.model.OrderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByOrderType(OrderType orderType);

    List<Order> findByTableId(Long tableId);

    List<Order> findByTableIdAndStatusNotIn(Long tableId, List<OrderStatus> statuses);

    List<Order> findAllByOrderByCreatedAtDesc();
}
