package com.humuson.orderintegration.service;

import com.humuson.orderintegration.domain.Order;
import com.humuson.orderintegration.domain.OrderStatus;

import java.util.List;

public interface OrderIntegrationService {
    /**
     * 외부 시스템에서 주문 데이터를 가져와 저장한다
     */
    List<Order> importOrdersFromExternal(String endpoint);

    /**
     * 주문 데이터를 외부 시스템으로 전송한다
     */
    boolean exportOrderToExternal(String endpoint, String orderId);

    /**
     * 여러 주문 데이터를 외부 시스템으로 전송한다
     */
    boolean exportOrdersToExternal(String endpoint, List<String> orderIds);

    /**
     * 모든 주문을 조회한다
     */
    List<Order> getAllOrders();

    /**
     * 주문 ID로 조회한다
     */
    Order getOrderById(String orderId);

    /**
     * 상태별 주문을 조회합니다
     */
    List<Order> getOrdersByStatus(OrderStatus status);
}

