package com.humuson.orderintegration.exception;

public class OrderNotFoundException extends DataIntegrationException {
    public OrderNotFoundException(String orderId) {
        super("주문을 찾을 수 없습니다: " + orderId);
    }
}
