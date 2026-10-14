package com.unipoli.kitchen.service;

import com.unipoli.kitchen.client.OrderServiceClient;
import com.unipoli.kitchen.dto.*;
import com.unipoli.kitchen.exception.InvalidKitchenStateException;
import com.unipoli.kitchen.exception.ResourceNotFoundException;
import com.unipoli.kitchen.model.*;
import com.unipoli.kitchen.repository.KitchenOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class KitchenService {

    private final KitchenOrderRepository repository;
    private final OrderServiceClient orderServiceClient;

    public KitchenService(KitchenOrderRepository repository, OrderServiceClient orderServiceClient) {
        this.repository = repository;
        this.orderServiceClient = orderServiceClient;
    }

    public KitchenOrderResponse receiveOrder(CreateKitchenOrderRequest request) {
        // Idempotency check: don't create duplicate kitchen ticket if already received
        if (repository.existsByOrderId(request.orderId())) {
            return toResponse(repository.findByOrderId(request.orderId()).get());
        }

        KitchenOrder kitchenOrder = new KitchenOrder();
        kitchenOrder.setOrderId(request.orderId());
        kitchenOrder.setOrderNumber(request.orderNumber());
        kitchenOrder.setTableId(request.tableId());
        kitchenOrder.setOrderType(request.orderType());
        kitchenOrder.setStatus(KitchenStatus.PENDING);
        kitchenOrder.setNotes(request.notes());

        for (CreateKitchenOrderRequest.KitchenItemRequest itemReq : request.items()) {
            KitchenOrderItem item = new KitchenOrderItem(
                    itemReq.dishId(),
                    itemReq.dishName(),
                    itemReq.quantity(),
                    itemReq.specialInstructions()
            );
            kitchenOrder.addItem(item);
        }

        KitchenOrder saved = repository.save(kitchenOrder);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<KitchenOrderResponse> findOrders(KitchenStatus status) {
        List<KitchenOrder> list;
        if (status != null) {
            list = repository.findByStatus(status);
        } else {
            // By default return active orders (PENDING, PREPARING) ordered by urgency
            list = repository.findByStatusInOrderByCreatedAtAsc(List.of(KitchenStatus.PENDING, KitchenStatus.PREPARING));
        }
        return list.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public KitchenOrderResponse findById(Long id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Kitchen order not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public KitchenOrderResponse findByOrderId(Long orderId) {
        return repository.findByOrderId(orderId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Kitchen order not found for order id: " + orderId));
    }

    public KitchenOrderResponse updateStatus(Long orderId, UpdateKitchenStatusRequest request) {
        KitchenOrder order = repository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Kitchen order not found for order id: " + orderId));

        if (order.getStatus() == KitchenStatus.READY) {
            throw new InvalidKitchenStateException("Order is already marked as READY");
        }
        if (order.getStatus() == KitchenStatus.CANCELLED) {
            throw new InvalidKitchenStateException("Cannot update status of a cancelled kitchen order");
        }

        if (request.status() == KitchenStatus.PREPARING) {
            order.setStatus(KitchenStatus.PREPARING);
            if (order.getStartedAt() == null) {
                order.setStartedAt(LocalDateTime.now());
            }
            return toResponse(repository.save(order));
        } else if (request.status() == KitchenStatus.READY) {
            return markAsReady(orderId);
        } else if (request.status() == KitchenStatus.CANCELLED) {
            order.setStatus(KitchenStatus.CANCELLED);
            return toResponse(repository.save(order));
        } else {
            order.setStatus(request.status());
            return toResponse(repository.save(order));
        }
    }

    public KitchenOrderResponse markAsReady(Long orderId) {
        KitchenOrder order = repository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Kitchen order not found for order id: " + orderId));

        if (order.getStatus() == KitchenStatus.READY) {
            return toResponse(order); // Idempotent
        }
        if (order.getStatus() == KitchenStatus.CANCELLED) {
            throw new InvalidKitchenStateException("Cannot mark a cancelled kitchen order as ready");
        }

        LocalDateTime now = LocalDateTime.now();
        order.setStatus(KitchenStatus.READY);
        order.setReadyAt(now);

        if (order.getStartedAt() == null) {
            order.setStartedAt(order.getCreatedAt() != null ? order.getCreatedAt() : now);
        }

        long minutes = Math.max(1, Duration.between(order.getStartedAt(), now).toMinutes());
        order.setPreparationTimeMinutes((int) minutes);

        KitchenOrder saved = repository.save(order);

        // Notify Order Service
        orderServiceClient.notifyOrderReady(saved.getOrderId(), saved.getOrderNumber());

        return toResponse(saved);
    }

    public KitchenOrderResponse toResponse(KitchenOrder order) {
        List<KitchenOrderResponse.KitchenItemResponse> itemResponses = order.getItems().stream()
                .map(i -> new KitchenOrderResponse.KitchenItemResponse(
                        i.getId(),
                        i.getDishId(),
                        i.getDishName(),
                        i.getQuantity(),
                        i.getSpecialInstructions()
                ))
                .toList();

        return new KitchenOrderResponse(
                order.getId(),
                order.getOrderId(),
                order.getOrderNumber(),
                order.getTableId(),
                order.getOrderType(),
                order.getStatus(),
                order.getNotes(),
                order.getStartedAt(),
                order.getReadyAt(),
                order.getPreparationTimeMinutes(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                itemResponses
        );
    }
}
