package com.unipoli.order.service;

import com.unipoli.order.client.MenuServiceClient;
import com.unipoli.order.client.OrderEventPublisher;
import com.unipoli.order.client.TableServiceClient;
import com.unipoli.order.dto.*;
import com.unipoli.order.exception.InvalidOrderStateException;
import com.unipoli.order.model.*;
import com.unipoli.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private MenuServiceClient menuServiceClient;

    @Mock
    private TableServiceClient tableServiceClient;

    @Mock
    private OrderEventPublisher eventPublisher;

    @InjectMocks
    private OrderService orderService;

    private DishInfo dish1;
    private TableInfo availableTable;
    private TableInfo occupiedTable;

    @BeforeEach
    void setUp() {
        dish1 = new DishInfo(
                1L,
                "Tacos al Pastor",
                "Delicious pork tacos",
                1L,
                "Tacos",
                new BigDecimal("120.00"),
                true
        );

        availableTable = new TableInfo(1L, "T-1", 4, "AVAILABLE", null);
        occupiedTable = new TableInfo(2L, "T-2", 4, "OCCUPIED", "ORD-123");
    }

    @Test
    void createOrder_WhenDineInAndValid_ShouldCalculateTaxesAndSave() {
        CreateOrderRequest request = new CreateOrderRequest(
                OrderType.DINE_IN,
                1L,
                10L,
                "Juan Perez",
                "555-0001",
                null,
                new BigDecimal("20.00"),
                "Extra salsa",
                List.of(new CreateOrderRequest.CreateOrderItemRequest(1L, 2, "Sin cebolla"))
        );

        when(tableServiceClient.getTableById(eq(1L), any())).thenReturn(availableTable);
        when(menuServiceClient.getDishesByIds(eq(List.of(1L)), any())).thenReturn(Map.of(1L, dish1));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> {
            Order o = i.getArgument(0);
            o.setId(100L);
            return o;
        });

        OrderResponse response = orderService.createOrder(request, "Bearer test-token");

        assertNotNull(response);
        assertEquals(OrderStatus.CONFIRMED, response.status());
        // Subtotal = 120 * 2 = 240.00
        assertEquals(new BigDecimal("240.00"), response.subtotal());
        // Tax = 240 * 0.16 = 38.40
        assertEquals(new BigDecimal("38.40"), response.tax());
        // Tip = 20.00
        assertEquals(new BigDecimal("20.00"), response.tip());
        // Total = 240 + 38.40 + 20.00 = 298.40
        assertEquals(new BigDecimal("298.40"), response.total());

        verify(tableServiceClient, times(1)).updateTableStatus(eq(1L), eq("OCCUPIED"), any());
        verify(eventPublisher, times(1)).publishOrderCreated(any());
    }

    @Test
    void createOrder_WhenTableOccupied_ShouldThrowInvalidOrderStateException() {
        CreateOrderRequest request = new CreateOrderRequest(
                OrderType.DINE_IN,
                2L,
                10L,
                "Juan Perez",
                "555-0001",
                null,
                BigDecimal.ZERO,
                null,
                List.of(new CreateOrderRequest.CreateOrderItemRequest(1L, 1, null))
        );

        when(tableServiceClient.getTableById(eq(2L), any())).thenReturn(occupiedTable);

        assertThrows(InvalidOrderStateException.class, () -> orderService.createOrder(request, "Bearer test-token"));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_WhenDishUnavailable_ShouldThrowInvalidOrderStateException() {
        DishInfo unavailableDish = new DishInfo(1L, "Tacos", "Desc", 1L, "Tacos", new BigDecimal("100.00"), false);

        CreateOrderRequest request = new CreateOrderRequest(
                OrderType.DELIVERY,
                null,
                null,
                "Maria",
                "555-9999",
                "Av Siempre Viva 123",
                BigDecimal.ZERO,
                null,
                List.of(new CreateOrderRequest.CreateOrderItemRequest(1L, 1, null))
        );

        when(menuServiceClient.getDishesByIds(eq(List.of(1L)), any())).thenReturn(Map.of(1L, unavailableDish));

        assertThrows(InvalidOrderStateException.class, () -> orderService.createOrder(request, "Bearer test-token"));
    }

    @Test
    void cancelOrder_WhenValid_ShouldSetCancelledAndFreeTable() {
        Order order = new Order();
        order.setId(10L);
        order.setOrderNumber("ORD-10");
        order.setStatus(OrderStatus.CONFIRMED);
        order.setTableId(1L);

        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderResponse response = orderService.cancelOrder(10L, "Bearer token");

        assertEquals(OrderStatus.CANCELLED, response.status());
        verify(tableServiceClient, times(1)).updateTableStatus(eq(1L), eq("AVAILABLE"), any());
    }

    @Test
    void handleOrderReady_ShouldUpdateStatusToReady() {
        Order order = new Order();
        order.setId(10L);
        order.setStatus(OrderStatus.IN_PREPARATION);

        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        orderService.handleOrderReady(10L);

        assertEquals(OrderStatus.READY, order.getStatus());
        verify(orderRepository, times(1)).save(order);
    }
}
