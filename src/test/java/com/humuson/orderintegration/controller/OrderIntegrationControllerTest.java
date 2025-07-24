package com.humuson.orderintegration.controller;

import com.humuson.orderintegration.controller.dto.ApiResponse;
import com.humuson.orderintegration.domain.Order;
import com.humuson.orderintegration.domain.OrderStatus;
import com.humuson.orderintegration.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderIntegrationControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 초기화
        orderRepository.save(createTestOrder("ORDER001", "고객1"));
        orderRepository.save(createTestOrder("ORDER002", "고객2"));
    }

    @Test
    void getAllOrders_성공() {
        // When
        ResponseEntity<ApiResponse> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/orders", ApiResponse.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
    }

    @Test
    void getOrder_성공() {
        // When
        ResponseEntity<ApiResponse> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/orders/ORDER001", ApiResponse.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
    }

    @Test
    void getOrder_존재하지않음() {
        // When
        ResponseEntity<ApiResponse> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/orders/NONEXISTENT", ApiResponse.class);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
    }

    private Order createTestOrder(String orderId, String customerName) {
        return Order.builder()
                .orderId(orderId)
                .customerName(customerName)
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.PROCESSING)
                .description("통합 테스트 주문")
                .build();
    }
}