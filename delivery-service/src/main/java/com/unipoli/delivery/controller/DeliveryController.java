package com.unipoli.delivery.controller;

import com.unipoli.delivery.dto.DeliveryOrderResponse;
import com.unipoli.delivery.dto.DeliveryWebhookOrderRequest;
import com.unipoli.delivery.dto.UpdateDeliveryStatusRequest;
import com.unipoli.delivery.model.DeliveryPlatform;
import com.unipoli.delivery.model.DeliveryStatus;
import com.unipoli.delivery.service.DeliveryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/delivery")
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @PostMapping("/webhooks/orders")
    public ResponseEntity<DeliveryOrderResponse> handleWebhookOrder(
            @Valid @RequestBody DeliveryWebhookOrderRequest request,
            @RequestHeader(value = "X-Delivery-Webhook-Secret", required = false) String webhookSecret
    ) {
        DeliveryOrderResponse response = deliveryService.processWebhookOrder(request, webhookSecret);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/orders")
    public ResponseEntity<List<DeliveryOrderResponse>> findAll(
            @RequestParam(required = false) DeliveryPlatform platform,
            @RequestParam(required = false) DeliveryStatus status
    ) {
        return ResponseEntity.ok(deliveryService.findAll(platform, status));
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<DeliveryOrderResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(deliveryService.findById(id));
    }

    @PutMapping("/orders/{id}/status")
    public ResponseEntity<DeliveryOrderResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDeliveryStatusRequest request
    ) {
        return ResponseEntity.ok(deliveryService.updateStatus(id, request));
    }
}
