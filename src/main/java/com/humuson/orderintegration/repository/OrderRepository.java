package com.humuson.orderintegration.repository;

import com.humuson.orderintegration.domain.Order;
import com.humuson.orderintegration.domain.OrderStatus;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(String orderId);
    List<Order> findAll();
    List<Order> findByStatus(OrderStatus status);
    boolean existsById(String orderId);
    void deleteById(String orderId);
}
