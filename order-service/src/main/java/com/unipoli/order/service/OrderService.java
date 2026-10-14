package com.unipoli.order.service;

import com.unipoli.order.client.MenuServiceClient;
import com.unipoli.order.client.OrderEventPublisher;
import com.unipoli.order.client.TableServiceClient;
import com.unipoli.order.dto.*;
import com.unipoli.order.exception.InvalidOrderStateException;
import com.unipoli.order.exception.ResourceNotFoundException;
import com.unipoli.order.model.*;
import com.unipoli.order.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Transactional
public class OrderService {

    private static final BigDecimal TAX_RATE = new BigDecimal("0.16");

    private final OrderRepository orderRepository;
    private final MenuServiceClient menuServiceClient;
    private final TableServiceClient tableServiceClient;
    private final OrderEventPublisher eventPublisher;

    public OrderService(
            OrderRepository orderRepository,
            MenuServiceClient menuServiceClient,
            TableServiceClient tableServiceClient,
            OrderEventPublisher eventPublisher
    ) {
        this.orderRepository = orderRepository;
        this.menuServiceClient = menuServiceClient;
        this.tableServiceClient = tableServiceClient;
        this.eventPublisher = eventPublisher;
    }

    public OrderResponse createOrder(CreateOrderRequest request, String authHeader) {
        // 1. Validate Table if DINE_IN
        if (request.orderType() == OrderType.DINE_IN) {
            if (request.tableId() == null) {
                throw new IllegalArgumentException("tableId is required for DINE_IN orders");
            }
            TableInfo table = tableServiceClient.getTableById(request.tableId(), authHeader);
            if ("OCCUPIED".equalsIgnoreCase(table.status())) {
                throw new InvalidOrderStateException("Table " + table.tableNumber() + " is currently occupied");
            }
        }

        // 2. Fetch and Validate Dishes from Menu Service
        List<Long> dishIds = request.items().stream()
                .map(CreateOrderRequest.CreateOrderItemRequest::dishId)
                .distinct()
                .toList();

        Map<Long, DishInfo> menuDishes = menuServiceClient.getDishesByIds(dishIds, authHeader);

        for (Long dishId : dishIds) {
            DishInfo dish = menuDishes.get(dishId);
            if (dish == null) {
                throw new IllegalArgumentException("Dish with id " + dishId + " does not exist in Menu");
            }
            if (Boolean.FALSE.equals(dish.available())) {
                throw new InvalidOrderStateException("Dish '" + dish.name() + "' is currently not available");
            }
        }

        // 3. Build Order and calculate financials
        Order order = new Order();
        String timestampPart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomPart = String.format("%04d", new Random().nextInt(10000));
        order.setOrderNumber("ORD-" + timestampPart + "-" + randomPart);
        order.setOrderType(request.orderType());
        order.setStatus(OrderStatus.CONFIRMED);
        order.setTableId(request.tableId());
        order.setWaiterId(request.waiterId());
        order.setCustomerName(request.customerName());
        order.setCustomerPhone(request.customerPhone());
        order.setDeliveryAddress(request.deliveryAddress());
        order.setNotes(request.notes());

        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderEvents.OrderItemEvent> eventItems = new ArrayList<>();

        for (CreateOrderRequest.CreateOrderItemRequest itemReq : request.items()) {
            DishInfo dish = menuDishes.get(itemReq.dishId());
            BigDecimal unitPrice = dish.price();

            OrderItem item = new OrderItem(
                    dish.dishId(),
                    dish.name(),
                    itemReq.quantity(),
                    unitPrice,
                    itemReq.specialInstructions()
            );
            order.addItem(item);

            subtotal = subtotal.add(item.getSubtotal());

            eventItems.add(new OrderEvents.OrderItemEvent(
                    dish.dishId(),
                    dish.name(),
                    itemReq.quantity(),
                    unitPrice,
                    itemReq.specialInstructions()
            ));
        }

        BigDecimal tax = subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal tip = request.tip() != null ? request.tip().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal total = subtotal.add(tax).add(tip);

        order.setSubtotal(subtotal);
        order.setTax(tax);
        order.setTip(tip);
        order.setTotal(total);

        Order saved = orderRepository.save(order);

        // 4. Update Table status to OCCUPIED if DINE_IN
        if (saved.getOrderType() == OrderType.DINE_IN && saved.getTableId() != null) {
            tableServiceClient.updateTableStatus(saved.getTableId(), "OCCUPIED", authHeader);
        }

        // 5. Publish OrderCreated Event to Kitchen and Inventory
        OrderEvents.OrderCreatedEvent event = new OrderEvents.OrderCreatedEvent(
                saved.getId(),
                saved.getOrderNumber(),
                saved.getOrderType(),
                saved.getStatus(),
                saved.getTableId(),
                saved.getWaiterId(),
                saved.getTotal(),
                saved.getNotes(),
                saved.getCreatedAt(),
                eventItems
        );
        eventPublisher.publishOrderCreated(event);

        return toOrderResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findAll(OrderStatus status, OrderType orderType, Long tableId) {
        List<Order> orders;
        if (status != null) {
            orders = orderRepository.findByStatus(status);
        } else if (orderType != null) {
            orders = orderRepository.findByOrderType(orderType);
        } else if (tableId != null) {
            orders = orderRepository.findByTableId(tableId);
        } else {
            orders = orderRepository.findAllByOrderByCreatedAtDesc();
        }
        return orders.stream().map(this::toOrderResponse).toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse findById(Long id) {
        return orderRepository.findById(id)
                .map(this::toOrderResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    public OrderResponse updateStatus(Long id, UpdateOrderStatusRequest request, String authHeader) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new InvalidOrderStateException("Cannot update status of a cancelled order");
        }
        if (order.getStatus() == OrderStatus.PAID && request.status() != OrderStatus.PAID) {
            throw new InvalidOrderStateException("Cannot modify status of an already paid order");
        }

        order.setStatus(request.status());

        if (request.status() == OrderStatus.PAID && order.getTableId() != null) {
            tableServiceClient.updateTableStatus(order.getTableId(), "AVAILABLE", authHeader);
            eventPublisher.publishOrderPaid(order.getId(), order.getOrderNumber(), order.getOrderType().name(),
                    order.getSubtotal(), order.getTax(), order.getTip(), order.getTotal());
        }

        Order updated = orderRepository.save(order);
        return toOrderResponse(updated);
    }

    public OrderResponse cancelOrder(Long id, String authHeader) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        if (order.getStatus() == OrderStatus.PAID) {
            throw new InvalidOrderStateException("Cannot cancel an order that has already been paid");
        }
        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new InvalidOrderStateException("Cannot cancel an order that has already been delivered");
        }

        order.setStatus(OrderStatus.CANCELLED);

        if (order.getTableId() != null) {
            tableServiceClient.updateTableStatus(order.getTableId(), "AVAILABLE", authHeader);
        }

        Order updated = orderRepository.save(order);
        return toOrderResponse(updated);
    }

    public void handleOrderReady(Long orderId) {
        orderRepository.findById(orderId).ifPresent(order -> {
            if (order.getStatus() != OrderStatus.CANCELLED && order.getStatus() != OrderStatus.PAID) {
                order.setStatus(OrderStatus.READY);
                orderRepository.save(order);
            }
        });
    }

    public void handlePaymentCompleted(Long orderId, OrderEvents.PaymentCompletedEvent event) {
        orderRepository.findById(orderId).ifPresent(order -> {
            order.setStatus(OrderStatus.PAID);
            orderRepository.save(order);

            if (order.getTableId() != null) {
                tableServiceClient.updateTableStatus(order.getTableId(), "AVAILABLE", null);
            }

            eventPublisher.publishOrderPaid(order.getId(), order.getOrderNumber(), order.getOrderType().name(),
                    order.getSubtotal(), order.getTax(), order.getTip(), order.getTotal());
        });
    }

    public OrderResponse toOrderResponse(Order order) {
        List<OrderResponse.OrderItemResponse> itemResponses = order.getItems().stream()
                .map(i -> new OrderResponse.OrderItemResponse(
                        i.getId(),
                        i.getDishId(),
                        i.getDishName(),
                        i.getQuantity(),
                        i.getUnitPrice(),
                        i.getSubtotal(),
                        i.getSpecialInstructions()
                ))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getOrderType(),
                order.getStatus(),
                order.getTableId(),
                order.getWaiterId(),
                order.getCustomerName(),
                order.getCustomerPhone(),
                order.getDeliveryAddress(),
                order.getSubtotal(),
                order.getTax(),
                order.getTip(),
                order.getTotal(),
                order.getNotes(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                itemResponses
        );
    }
}
