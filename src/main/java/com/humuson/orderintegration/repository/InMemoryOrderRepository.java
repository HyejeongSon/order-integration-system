package com.humuson.orderintegration.repository;

import com.humuson.orderintegration.domain.Order;
import com.humuson.orderintegration.domain.OrderStatus;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class InMemoryOrderRepository implements OrderRepository {
    private final Map<String, Order> orderStore = new ConcurrentHashMap<>();

    @Override
    public Order save(Order order) {
        if (order == null || order.getOrderId() == null) {
            throw new IllegalArgumentException("주문 또는 주문 ID가 null입니다");
        }
        orderStore.put(order.getOrderId(), order);
        return order;
    }

    @Override
    public Optional<Order> findById(String orderId) {
        return Optional.ofNullable(orderStore.get(orderId));
    }

    @Override
    public List<Order> findAll() {
        return new ArrayList<>(orderStore.values());
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        return orderStore.values().stream()
                .filter(order -> order.getStatus() == status)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(String orderId) {
        return orderStore.containsKey(orderId);
    }

    @Override
    public void deleteById(String orderId) {
        orderStore.remove(orderId);
    }
}
