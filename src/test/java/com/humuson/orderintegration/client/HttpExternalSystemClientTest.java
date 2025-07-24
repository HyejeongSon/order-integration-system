package com.humuson.orderintegration.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.humuson.orderintegration.client.dto.ExternalSystemResponse;
import com.humuson.orderintegration.client.dto.OrderRequest;
import com.humuson.orderintegration.domain.Order;
import com.humuson.orderintegration.domain.OrderStatus;
import com.humuson.orderintegration.exception.ExternalSystemException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HttpExternalSystemClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    private HttpExternalSystemClient httpClient;

    @BeforeEach
    void setUp() {
        httpClient = new HttpExternalSystemClient(restTemplate, objectMapper);
    }

    @Test
    void fetchOrders_성공() throws Exception {
        // Given
        String endpoint = "http://external-system.com/orders";
        String responseBody = "[{\"orderId\":\"ORDER001\",\"customerName\":\"고객1\",\"orderDate\":\"2024-01-01 10:00:00\",\"status\":\"PROCESSING\"}]";

        ResponseEntity<String> mockResponse = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.exchange(eq(endpoint), eq(HttpMethod.GET), any(), eq(String.class)))
                .thenReturn(mockResponse);

        List<OrderRequest> mockRequests = Arrays.asList(
                OrderRequest.builder()
                        .orderId("ORDER001")
                        .customerName("고객1")
                        .orderDate("2024-01-01 10:00:00")
                        .status("PROCESSING")
                        .build()
        );
        when(objectMapper.readValue(eq(responseBody), any(TypeReference.class)))
                .thenReturn(mockRequests);

        // When
        List<Order> result = httpClient.fetchOrders(endpoint);

        // Then
        assertEquals(1, result.size());
        assertEquals("ORDER001", result.get(0).getOrderId());
        verify(restTemplate).exchange(eq(endpoint), eq(HttpMethod.GET), any(), eq(String.class));
    }

    @Test
    void fetchOrders_HTTP_오류() {
        // Given
        String endpoint = "http://external-system.com/orders";
        when(restTemplate.exchange(eq(endpoint), eq(HttpMethod.GET), any(), eq(String.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        // When & Then
        ExternalSystemException exception = assertThrows(ExternalSystemException.class,
                () -> httpClient.fetchOrders(endpoint));
        assertTrue(exception.getMessage().contains("HTTP"));
        assertTrue(exception.getMessage().contains("400"));
    }

    @Test
    void fetchOrders_네트워크_오류() {
        // Given
        String endpoint = "http://external-system.com/orders";
        when(restTemplate.exchange(eq(endpoint), eq(HttpMethod.GET), any(), eq(String.class)))
                .thenThrow(new ResourceAccessException("Connection timeout"));

        // When & Then
        ExternalSystemException exception = assertThrows(ExternalSystemException.class,
                () -> httpClient.fetchOrders(endpoint));
        assertTrue(exception.getMessage().contains("네트워크 연결 오류"));
    }

    @Test
    void sendOrder_성공() {
        // Given
        String endpoint = "http://external-system.com/orders";
        Order testOrder = Order.builder()
                .orderId("ORDER001")
                .customerName("고객1")
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.PROCESSING)
                .build();

        ExternalSystemResponse<Object> mockResponse = ExternalSystemResponse.builder()
                .success(true)
                .message("성공")
                .build();

        ResponseEntity<ExternalSystemResponse> mockResponseEntity =
                new ResponseEntity<>(mockResponse, HttpStatus.OK);

        when(restTemplate.exchange(eq(endpoint), eq(HttpMethod.POST), any(), eq(ExternalSystemResponse.class)))
                .thenReturn(mockResponseEntity);

        // When
        boolean result = httpClient.sendOrder(endpoint, testOrder);

        // Then
        assertTrue(result);
        verify(restTemplate).exchange(eq(endpoint), eq(HttpMethod.POST), any(), eq(ExternalSystemResponse.class));
    }
}