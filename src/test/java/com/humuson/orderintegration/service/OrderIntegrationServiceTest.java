package com.humuson.orderintegration.service;

import com.humuson.orderintegration.client.ExternalSystemClient;
import com.humuson.orderintegration.domain.Order;
import com.humuson.orderintegration.domain.OrderStatus;
import com.humuson.orderintegration.exception.ExternalSystemException;
import com.humuson.orderintegration.exception.OrderNotFoundException;
import com.humuson.orderintegration.repository.OrderRepository;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderIntegrationServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ExternalSystemClient externalSystemClient;

    @Mock
    private RestTemplate restTemplate;

    private OrderIntegrationService orderIntegrationService;
    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        orderIntegrationService = new OrderIntegrationServiceImpl(
                orderRepository, externalSystemClient, validator);
    }

    @Test
    void importOrdersFromExternal_성공() {
        // Given
        String endpoint = "http://external-system.com/orders";
        List<Order> mockOrders = Arrays.asList(
                createTestOrder("ORDER001", "고객1"),
                createTestOrder("ORDER002", "고객2")
        );

        when(externalSystemClient.fetchOrders(endpoint)).thenReturn(mockOrders);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        List<Order> result = orderIntegrationService.importOrdersFromExternal(endpoint);

        // Then
        assertEquals(2, result.size());
        verify(externalSystemClient).fetchOrders(endpoint);
        verify(orderRepository, times(2)).save(any(Order.class));
    }

    @Test
    void importOrdersFromExternal_외부시스템_오류() {
        // Given
        String endpoint = "http://external-system.com/orders";
        when(externalSystemClient.fetchOrders(endpoint))
                .thenThrow(new ExternalSystemException("HTTP", "네트워크 오류"));

        // When & Then
        assertThrows(ExternalSystemException.class,
                () -> orderIntegrationService.importOrdersFromExternal(endpoint));
    }

    @Test
    void exportOrderToExternal_성공() {
        // Given
        String endpoint = "http://external-system.com/orders";
        String orderId = "ORDER001";
        Order testOrder = createTestOrder(orderId, "고객1");

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
        when(externalSystemClient.sendOrder(endpoint, testOrder)).thenReturn(true);

        // When
        boolean result = orderIntegrationService.exportOrderToExternal(endpoint, orderId);

        // Then
        assertTrue(result);
        verify(orderRepository).findById(orderId);
        verify(externalSystemClient).sendOrder(endpoint, testOrder);
    }

    @Test
    void exportOrderToExternal_주문없음() {
        // Given
        String endpoint = "http://external-system.com/orders";
        String orderId = "NONEXISTENT";

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(OrderNotFoundException.class,
                () -> orderIntegrationService.exportOrderToExternal(endpoint, orderId));
    }

    @Test
    void getAllOrders_성공() {
        // Given
        List<Order> mockOrders = Arrays.asList(
                createTestOrder("ORDER001", "고객1"),
                createTestOrder("ORDER002", "고객2")
        );
        when(orderRepository.findAll()).thenReturn(mockOrders);

        // When
        List<Order> result = orderIntegrationService.getAllOrders();

        // Then
        assertEquals(2, result.size());
        verify(orderRepository).findAll();
    }

    @Test
    void getOrderById_성공() {
        // Given
        String orderId = "ORDER001";
        Order testOrder = createTestOrder(orderId, "고객1");
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));

        // When
        Order result = orderIntegrationService.getOrderById(orderId);

        // Then
        assertNotNull(result);
        assertEquals(orderId, result.getOrderId());
        verify(orderRepository).findById(orderId);
    }

    @Test
    void getOrderById_주문없음() {
        // Given
        String orderId = "NONEXISTENT";
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(OrderNotFoundException.class,
                () -> orderIntegrationService.getOrderById(orderId));
    }

    private Order createTestOrder(String orderId, String customerName) {
        return Order.builder()
                .orderId(orderId)
                .customerName(customerName)
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.PROCESSING)
                .description("테스트 주문")
                .build();
    }
}