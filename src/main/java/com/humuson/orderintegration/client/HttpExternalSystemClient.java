package com.humuson.orderintegration.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.humuson.orderintegration.client.dto.ExternalSystemResponse;
import com.humuson.orderintegration.client.dto.OrderRequest;
import com.humuson.orderintegration.client.dto.OrderResponse;
import com.humuson.orderintegration.domain.Order;
import com.humuson.orderintegration.domain.OrderStatus;
import com.humuson.orderintegration.exception.ExternalSystemException;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class HttpExternalSystemClient implements ExternalSystemClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private static final String SYSTEM_TYPE = "HTTP";

    public HttpExternalSystemClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<Order> fetchOrders(String endpoint) throws ExternalSystemException {
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(endpoint, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() != HttpStatus.OK) {
                throw new ExternalSystemException(SYSTEM_TYPE, "HTTP 요청 실패: " + response.getStatusCode());
            }

            return parseOrdersFromResponse(response.getBody());

        } catch (HttpClientErrorException e) {
            throw new ExternalSystemException(SYSTEM_TYPE, "HTTP 클라이언트 오류: " + e.getStatusCode(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalSystemException(SYSTEM_TYPE, "네트워크 연결 오류", e);
        } catch (Exception e) {
            throw new ExternalSystemException(SYSTEM_TYPE, "데이터 조회 중 오류 발생", e);
        }
    }

    @Override
    public boolean sendOrder(String endpoint, Order order) throws ExternalSystemException {
        return sendOrders(endpoint, Arrays.asList(order));
    }

    @Override
    public boolean sendOrders(String endpoint, List<Order> orders) throws ExternalSystemException {
        try {
            List<OrderResponse> orderResponses = orders.stream()
                    .map(this::convertToOrderResponse)
                    .collect(Collectors.toList());

            HttpHeaders headers = createHeaders();
            HttpEntity<List<OrderResponse>> entity = new HttpEntity<>(orderResponses, headers);

            ResponseEntity<ExternalSystemResponse> response = restTemplate.exchange(
                    endpoint, HttpMethod.POST, entity, ExternalSystemResponse.class);

            if (response.getStatusCode() != HttpStatus.OK) {
                throw new ExternalSystemException(SYSTEM_TYPE, "데이터 전송 실패: " + response.getStatusCode());
            }

            ExternalSystemResponse<?> responseBody = response.getBody();
            return responseBody != null && responseBody.isSuccess();

        } catch (HttpClientErrorException e) {
            throw new ExternalSystemException(SYSTEM_TYPE, "HTTP 클라이언트 오류: " + e.getStatusCode(), e);
        } catch (ResourceAccessException e) {
            throw new ExternalSystemException(SYSTEM_TYPE, "네트워크 연결 오류", e);
        } catch (Exception e) {
            throw new ExternalSystemException(SYSTEM_TYPE, "데이터 전송 중 오류 발생", e);
        }
    }

    @Override
    public String getSystemType() {
        return SYSTEM_TYPE;
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        return headers;
    }

    private List<Order> parseOrdersFromResponse(String responseBody) throws Exception {
        try {
            // 외부 시스템에서 OrderRequest 형태로 데이터가 온다고 가정
            List<OrderRequest> orderRequests = objectMapper.readValue(
                    responseBody, new TypeReference<List<OrderRequest>>() {});

            return orderRequests.stream()
                    .map(this::convertToOrder)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new ExternalSystemException(SYSTEM_TYPE, "응답 데이터 파싱 오류", e);
        }
    }

    private Order convertToOrder(OrderRequest request) {
        return Order.builder()
                .orderId(request.getOrderId())
                .customerName(request.getCustomerName())
                .orderDate(parseDateTime(request.getOrderDate()))
                .status(OrderStatus.valueOf(request.getStatus().toUpperCase()))
                .description(request.getDescription())
                .build();
    }

    private OrderResponse convertToOrderResponse(Order order) {
        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .customerName(order.getCustomerName())
                .orderDate(formatDateTime(order.getOrderDate()))
                .status(order.getStatus().name())
                .description(order.getDescription())
                .processedAt(formatDateTime(LocalDateTime.now()))
                .build();
    }

    private LocalDateTime parseDateTime(String dateTime) {
        try {
            return LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
