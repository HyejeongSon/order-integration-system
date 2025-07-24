package com.humuson.orderintegration.repository;

import com.humuson.orderintegration.domain.Order;
import com.humuson.orderintegration.domain.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class InMemoryOrderRepositoryTest {
    private InMemoryOrderRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryOrderRepository();
    }

    @Test
    void save_성공() {
        // Given
        Order order = createTestOrder("ORDER001", "고객1");

        // When
        Order savedOrder = repository.save(order);

        // Then
        assertNotNull(savedOrder);
        assertEquals("ORDER001", savedOrder.getOrderId());
        assertTrue(repository.existsById("ORDER001"));
    }

    @Test
    void save_null_주문() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> repository.save(null));
    }

    @Test
    void findById_성공() {
        // Given
        Order order = createTestOrder("ORDER001", "고객1");
        repository.save(order);

        // When
        Optional<Order> found = repository.findById("ORDER001");

        // Then
        assertTrue(found.isPresent());
        assertEquals("ORDER001", found.get().getOrderId());
    }

    @Test
    void findById_존재하지_않음() {
        // When
        Optional<Order> found = repository.findById("NONEXISTENT");

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    void findAll_성공() {
        // Given
        repository.save(createTestOrder("ORDER001", "고객1"));
        repository.save(createTestOrder("ORDER002", "고객2"));

        // When
        List<Order> orders = repository.findAll();

        // Then
        assertEquals(2, orders.size());
    }

    @Test
    void findByStatus_성공() {
        // Given
        repository.save(createTestOrder("ORDER001", "고객1", OrderStatus.PROCESSING));
        repository.save(createTestOrder("ORDER002", "고객2", OrderStatus.COMPLETED));
        repository.save(createTestOrder("ORDER003", "고객3", OrderStatus.PROCESSING));

        // When
        List<Order> processingOrders = repository.findByStatus(OrderStatus.PROCESSING);

        // Then
        assertEquals(2, processingOrders.size());
        assertTrue(processingOrders.stream()
                .allMatch(order -> order.getStatus() == OrderStatus.PROCESSING));
    }

    @Test
    void deleteById_성공() {
        // Given
        Order order = createTestOrder("ORDER001", "고객1");
        repository.save(order);
        assertTrue(repository.existsById("ORDER001"));

        // When
        repository.deleteById("ORDER001");

        // Then
        assertFalse(repository.existsById("ORDER001"));
    }

    private Order createTestOrder(String orderId, String customerName) {
        return createTestOrder(orderId, customerName, OrderStatus.PROCESSING);
    }

    private Order createTestOrder(String orderId, String customerName, OrderStatus status) {
        return Order.builder()
                .orderId(orderId)
                .customerName(customerName)
                .orderDate(LocalDateTime.now())
                .status(status)
                .description("테스트 주문")
                .build();
    }
}