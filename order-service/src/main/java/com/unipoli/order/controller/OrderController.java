package com.unipoli.order.controller;

import com.unipoli.order.dto.CreateOrderRequest;
import com.unipoli.order.dto.OrderEvents;
import com.unipoli.order.dto.OrderResponse;
import com.unipoli.order.dto.UpdateOrderStatusRequest;
import com.unipoli.order.model.OrderStatus;
import com.unipoli.order.model.OrderType;
import com.unipoli.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(
            @Valid @RequestBody CreateOrderRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        OrderResponse response = orderService.createOrder(request, authHeader);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> findAll(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) OrderType orderType,
            @RequestParam(required = false) Long tableId
    ) {
        return ResponseEntity.ok(orderService.findAll(status, orderType, tableId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.findById(id));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        return ResponseEntity.ok(orderService.updateStatus(id, request, authHeader));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancel(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        return ResponseEntity.ok(orderService.cancelOrder(id, authHeader));
    }

    @PostMapping("/events/order-ready")
    public ResponseEntity<Map<String, String>> handleOrderReady(
            @RequestBody OrderEvents.OrderReadyEvent event
    ) {
        orderService.handleOrderReady(event.orderId());
        return ResponseEntity.ok(Map.of("status", "SUCCESS", "message", "Order marked as READY"));
    }

    @PostMapping("/events/payment-completed")
    public ResponseEntity<Map<String, String>> handlePaymentCompleted(
            @RequestBody OrderEvents.PaymentCompletedEvent event
    ) {
        orderService.handlePaymentCompleted(event.orderId(), event);
        return ResponseEntity.ok(Map.of("status", "SUCCESS", "message", "Order marked as PAID"));
    }
}
