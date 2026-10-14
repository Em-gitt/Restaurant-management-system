package com.unipoli.delivery.service;

import com.unipoli.delivery.client.OrderServiceClient;
import com.unipoli.delivery.dto.DeliveryOrderResponse;
import com.unipoli.delivery.dto.DeliveryWebhookOrderRequest;
import com.unipoli.delivery.dto.InternalOrderCreatedResponse;
import com.unipoli.delivery.exception.DuplicateWebhookException;
import com.unipoli.delivery.exception.UnauthorizedWebhookException;
import com.unipoli.delivery.model.DeliveryOrder;
import com.unipoli.delivery.model.DeliveryPlatform;
import com.unipoli.delivery.model.DeliveryStatus;
import com.unipoli.delivery.repository.DeliveryOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

    @Mock
    private DeliveryOrderRepository repository;

    @Mock
    private OrderServiceClient orderServiceClient;

    private DeliveryService service;

    private final String secret = "test-webhook-secret";

    @BeforeEach
    void setUp() {
        service = new DeliveryService(repository, orderServiceClient, secret);
    }

    @Test
    void processWebhookOrder_WhenValidRappi_ShouldCalculate18PercentCommissionAndSave() {
        DeliveryWebhookOrderRequest request = new DeliveryWebhookOrderRequest(
                DeliveryPlatform.RAPPI,
                "RAPPI-999",
                "Laura Sanchez",
                "555-4321",
                "Calle 10 #20",
                new BigDecimal("500.00"),
                "Doorbell broken",
                List.of(new DeliveryWebhookOrderRequest.DeliveryOrderItemRequest(1L, 2, "Sin cebolla"))
        );

        when(repository.existsByPlatformAndExternalOrderId(DeliveryPlatform.RAPPI, "RAPPI-999")).thenReturn(false);
        when(orderServiceClient.createDeliveryOrder(any())).thenReturn(
                new InternalOrderCreatedResponse(50L, "ORD-50", "CONFIRMED", new BigDecimal("500.00"))
        );
        when(repository.save(any(DeliveryOrder.class))).thenAnswer(i -> {
            DeliveryOrder d = i.getArgument(0);
            d.setId(1L);
            d.setCreatedAt(LocalDateTime.now());
            return d;
        });

        DeliveryOrderResponse response = service.processWebhookOrder(request, secret);

        assertNotNull(response);
        assertEquals(DeliveryPlatform.RAPPI, response.platform());
        assertEquals("RAPPI-999", response.externalOrderId());
        assertEquals(50L, response.internalOrderId());
        assertEquals(new BigDecimal("18.00"), response.commissionRate());
        // Commission = 500 * 18% = 90.00
        assertEquals(new BigDecimal("90.00"), response.commissionAmount());
        // Net = 500 - 90 = 410.00
        assertEquals(new BigDecimal("410.00"), response.netPayout());
        assertEquals(DeliveryStatus.ORDER_CREATED, response.status());

        verify(repository, times(1)).save(any(DeliveryOrder.class));
    }

    @Test
    void processWebhookOrder_WhenInvalidSecret_ShouldThrowUnauthorizedWebhookException() {
        DeliveryWebhookOrderRequest request = new DeliveryWebhookOrderRequest(
                DeliveryPlatform.RAPPI,
                "RAPPI-999",
                "Laura Sanchez",
                "555-4321",
                "Calle 10 #20",
                new BigDecimal("500.00"),
                null,
                List.of(new DeliveryWebhookOrderRequest.DeliveryOrderItemRequest(1L, 1, null))
        );

        assertThrows(UnauthorizedWebhookException.class, () -> service.processWebhookOrder(request, "wrong-secret"));
        verify(repository, never()).save(any());
    }

    @Test
    void processWebhookOrder_WhenDuplicateOrder_ShouldThrowDuplicateWebhookException() {
        DeliveryWebhookOrderRequest request = new DeliveryWebhookOrderRequest(
                DeliveryPlatform.DIDI_FOOD,
                "DIDI-100",
                "Mario",
                "555-8888",
                "Calle 5 #10",
                new BigDecimal("300.00"),
                null,
                List.of(new DeliveryWebhookOrderRequest.DeliveryOrderItemRequest(1L, 1, null))
        );

        when(repository.existsByPlatformAndExternalOrderId(DeliveryPlatform.DIDI_FOOD, "DIDI-100")).thenReturn(true);

        assertThrows(DuplicateWebhookException.class, () -> service.processWebhookOrder(request, secret));
    }
}
