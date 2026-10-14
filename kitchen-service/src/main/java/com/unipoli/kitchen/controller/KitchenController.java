package com.unipoli.kitchen.controller;

import com.unipoli.kitchen.dto.CreateKitchenOrderRequest;
import com.unipoli.kitchen.dto.KitchenOrderResponse;
import com.unipoli.kitchen.dto.UpdateKitchenStatusRequest;
import com.unipoli.kitchen.model.KitchenStatus;
import com.unipoli.kitchen.service.KitchenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/kitchen/orders")
public class KitchenController {

    private final KitchenService kitchenService;

    public KitchenController(KitchenService kitchenService) {
        this.kitchenService = kitchenService;
    }

    @PostMapping
    public ResponseEntity<KitchenOrderResponse> receiveOrder(@Valid @RequestBody CreateKitchenOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(kitchenService.receiveOrder(request));
    }

    @GetMapping
    public ResponseEntity<List<KitchenOrderResponse>> findOrders(
            @RequestParam(required = false) KitchenStatus status
    ) {
        return ResponseEntity.ok(kitchenService.findOrders(status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<KitchenOrderResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(kitchenService.findById(id));
    }

    @GetMapping("/by-order/{orderId}")
    public ResponseEntity<KitchenOrderResponse> findByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(kitchenService.findByOrderId(orderId));
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<KitchenOrderResponse> updateStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateKitchenStatusRequest request
    ) {
        return ResponseEntity.ok(kitchenService.updateStatus(orderId, request));
    }

    @PutMapping("/{order_id}/ready")
    public ResponseEntity<KitchenOrderResponse> markAsReady(@PathVariable("order_id") Long orderId) {
        return ResponseEntity.ok(kitchenService.markAsReady(orderId));
    }
}
