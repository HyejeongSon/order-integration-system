package com.humuson.orderintegration.client;

import com.humuson.orderintegration.domain.Order;
import com.humuson.orderintegration.exception.ExternalSystemException;

import java.util.List;

public interface ExternalSystemClient {
    /**
     * 외부 시스템에서 주문 데이터를 가져온다
     */
    List<Order> fetchOrders(String endpoint) throws ExternalSystemException;

    /**
     * 외부 시스템으로 주문 데이터를 전송한다
     */
    boolean sendOrder(String endpoint, Order order) throws ExternalSystemException;

    /**
     * 외부 시스템으로 여러 주문 데이터를 전송한다
     */
    boolean sendOrders(String endpoint, List<Order> orders) throws ExternalSystemException;

    /**
     * 시스템 타입을 반환한다
     */
    String getSystemType();
}