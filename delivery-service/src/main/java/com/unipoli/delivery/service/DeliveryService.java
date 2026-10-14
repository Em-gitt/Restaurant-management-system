package com.unipoli.delivery.service;

import com.unipoli.delivery.client.OrderServiceClient;
import com.unipoli.delivery.dto.*;
import com.unipoli.delivery.exception.DuplicateWebhookException;
import com.unipoli.delivery.exception.ResourceNotFoundException;
import com.unipoli.delivery.exception.UnauthorizedWebhookException;
import com.unipoli.delivery.model.*;
import com.unipoli.delivery.repository.DeliveryOrderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@Transactional
public class DeliveryService {

    private final DeliveryOrderRepository repository;
    private final OrderServiceClient orderServiceClient;
    private final String webhookSecret;

    public DeliveryService(
            DeliveryOrderRepository repository,
            OrderServiceClient orderServiceClient,
            @Value("${delivery.webhook.secret:secret-delivery-webhook-key-restaurant}") String webhookSecret
    ) {
        this.repository = repository;
        this.orderServiceClient = orderServiceClient;
        this.webhookSecret = webhookSecret;
    }

    public DeliveryOrderResponse processWebhookOrder(DeliveryWebhookOrderRequest request, String providedSecret) {
        // 1. Validate Webhook Secret
        if (providedSecret == null || !providedSecret.equals(webhookSecret)) {
            throw new UnauthorizedWebhookException("Invalid or missing X-Delivery-Webhook-Secret header");
        }

        // 2. Idempotency Check
        if (repository.existsByPlatformAndExternalOrderId(request.platform(), request.externalOrderId())) {
            throw new DuplicateWebhookException("Webhook order for platform " + request.platform() +
                    " with ID " + request.externalOrderId() + " has already been processed");
        }

        // 3. Calculate Platform Commission
        BigDecimal commissionRate = switch (request.platform()) {
            case RAPPI -> new BigDecimal("18.00");
            case DIDI_FOOD -> new BigDecimal("15.00");
            case UBER_EATS -> new BigDecimal("20.00");
        };

        BigDecimal commissionAmount = request.orderTotal()
                .multiply(commissionRate)
                .divide(new BigDecimal("100.00"), 2, RoundingMode.HALF_UP);

        BigDecimal netPayout = request.orderTotal().subtract(commissionAmount);

        // 4. Create Order via Order Service
        InternalOrderCreatedResponse internalOrder = orderServiceClient.createDeliveryOrder(request);

        // 5. Persist Delivery Order
        DeliveryOrder deliveryOrder = new DeliveryOrder();
        deliveryOrder.setPlatform(request.platform());
        deliveryOrder.setExternalOrderId(request.externalOrderId());
        deliveryOrder.setInternalOrderId(internalOrder != null ? internalOrder.id() : null);
        deliveryOrder.setCustomerName(request.customerName());
        deliveryOrder.setCustomerPhone(request.customerPhone());
        deliveryOrder.setDeliveryAddress(request.deliveryAddress());
        deliveryOrder.setOrderTotal(request.orderTotal());
        deliveryOrder.setCommissionRate(commissionRate);
        deliveryOrder.setCommissionAmount(commissionAmount);
        deliveryOrder.setNetPayout(netPayout);
        deliveryOrder.setStatus(DeliveryStatus.ORDER_CREATED);
        deliveryOrder.setRawPayload(request.toString());

        DeliveryOrder saved = repository.save(deliveryOrder);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<DeliveryOrderResponse> findAll(DeliveryPlatform platform, DeliveryStatus status) {
        List<DeliveryOrder> list;
        if (platform != null) {
            list = repository.findByPlatform(platform);
        } else if (status != null) {
            list = repository.findByStatus(status);
        } else {
            list = repository.findAllByOrderByCreatedAtDesc();
        }
        return list.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public DeliveryOrderResponse findById(Long id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery order not found with id: " + id));
    }

    public DeliveryOrderResponse updateStatus(Long id, UpdateDeliveryStatusRequest request) {
        DeliveryOrder order = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery order not found with id: " + id));

        order.setStatus(request.status());
        DeliveryOrder saved = repository.save(order);
        return toResponse(saved);
    }

    public DeliveryOrderResponse toResponse(DeliveryOrder order) {
        return new DeliveryOrderResponse(
                order.getId(),
                order.getPlatform(),
                order.getExternalOrderId(),
                order.getInternalOrderId(),
                order.getCustomerName(),
                order.getCustomerPhone(),
                order.getDeliveryAddress(),
                order.getOrderTotal(),
                order.getCommissionRate(),
                order.getCommissionAmount(),
                order.getNetPayout(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
