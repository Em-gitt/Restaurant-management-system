package com.unipoli.kitchen.service;

import com.unipoli.kitchen.client.OrderServiceClient;
import com.unipoli.kitchen.dto.CreateKitchenOrderRequest;
import com.unipoli.kitchen.dto.KitchenOrderResponse;
import com.unipoli.kitchen.dto.UpdateKitchenStatusRequest;
import com.unipoli.kitchen.exception.InvalidKitchenStateException;
import com.unipoli.kitchen.model.KitchenOrder;
import com.unipoli.kitchen.model.KitchenOrderItem;
import com.unipoli.kitchen.model.KitchenStatus;
import com.unipoli.kitchen.repository.KitchenOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KitchenServiceTest {

    @Mock
    private KitchenOrderRepository repository;

    @Mock
    private OrderServiceClient orderServiceClient;

    @InjectMocks
    private KitchenService service;

    private KitchenOrder kitchenOrder;

    @BeforeEach
    void setUp() {
        kitchenOrder = new KitchenOrder();
        kitchenOrder.setId(1L);
        kitchenOrder.setOrderId(100L);
        kitchenOrder.setOrderNumber("ORD-2026-100");
        kitchenOrder.setTableId(5L);
        kitchenOrder.setOrderType("DINE_IN");
        kitchenOrder.setStatus(KitchenStatus.PENDING);
        kitchenOrder.setCreatedAt(LocalDateTime.now().minusMinutes(10));
        kitchenOrder.addItem(new KitchenOrderItem(1L, "Enchiladas", 2, "Con crema extra"));
    }

    @Test
    void receiveOrder_WhenNew_ShouldSaveAndReturnResponse() {
        CreateKitchenOrderRequest request = new CreateKitchenOrderRequest(
                100L,
                "ORD-2026-100",
                5L,
                "DINE_IN",
                "None",
                List.of(new CreateKitchenOrderRequest.KitchenItemRequest(1L, "Enchiladas", 2, "Con crema extra"))
        );

        when(repository.existsByOrderId(100L)).thenReturn(false);
        when(repository.save(any(KitchenOrder.class))).thenReturn(kitchenOrder);

        KitchenOrderResponse response = service.receiveOrder(request);

        assertNotNull(response);
        assertEquals(KitchenStatus.PENDING, response.status());
        assertEquals("ORD-2026-100", response.orderNumber());
        verify(repository, times(1)).save(any(KitchenOrder.class));
    }

    @Test
    void receiveOrder_WhenAlreadyExists_ShouldReturnExistingIdempotently() {
        CreateKitchenOrderRequest request = new CreateKitchenOrderRequest(
                100L,
                "ORD-2026-100",
                5L,
                "DINE_IN",
                "None",
                List.of(new CreateKitchenOrderRequest.KitchenItemRequest(1L, "Enchiladas", 2, "Con crema extra"))
        );

        when(repository.existsByOrderId(100L)).thenReturn(true);
        when(repository.findByOrderId(100L)).thenReturn(Optional.of(kitchenOrder));

        KitchenOrderResponse response = service.receiveOrder(request);

        assertNotNull(response);
        verify(repository, never()).save(any(KitchenOrder.class));
    }

    @Test
    void updateStatus_ToPreparing_ShouldSetStartedAt() {
        when(repository.findByOrderId(100L)).thenReturn(Optional.of(kitchenOrder));
        when(repository.save(any(KitchenOrder.class))).thenReturn(kitchenOrder);

        UpdateKitchenStatusRequest request = new UpdateKitchenStatusRequest(KitchenStatus.PREPARING);

        KitchenOrderResponse response = service.updateStatus(100L, request);

        assertNotNull(response);
        assertEquals(KitchenStatus.PREPARING, kitchenOrder.getStatus());
        assertNotNull(kitchenOrder.getStartedAt());
    }

    @Test
    void markAsReady_WhenPending_ShouldSetReadyAndNotifyOrderService() {
        when(repository.findByOrderId(100L)).thenReturn(Optional.of(kitchenOrder));
        when(repository.save(any(KitchenOrder.class))).thenReturn(kitchenOrder);

        KitchenOrderResponse response = service.markAsReady(100L);

        assertEquals(KitchenStatus.READY, kitchenOrder.getStatus());
        assertNotNull(kitchenOrder.getReadyAt());
        assertNotNull(kitchenOrder.getPreparationTimeMinutes());
        verify(orderServiceClient, times(1)).notifyOrderReady(100L, "ORD-2026-100");
    }

    @Test
    void markAsReady_WhenCancelled_ShouldThrowException() {
        kitchenOrder.setStatus(KitchenStatus.CANCELLED);
        when(repository.findByOrderId(100L)).thenReturn(Optional.of(kitchenOrder));

        assertThrows(InvalidKitchenStateException.class, () -> service.markAsReady(100L));
    }
}
