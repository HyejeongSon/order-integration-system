package com.humuson.orderintegration.service;

import com.humuson.orderintegration.client.ExternalSystemClient;
import com.humuson.orderintegration.domain.Order;
import com.humuson.orderintegration.domain.OrderStatus;
import com.humuson.orderintegration.exception.DataIntegrationException;
import com.humuson.orderintegration.exception.ExternalSystemException;
import com.humuson.orderintegration.exception.OrderNotFoundException;
import com.humuson.orderintegration.repository.OrderRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderIntegrationServiceImpl implements OrderIntegrationService {

    private static final Logger logger = LoggerFactory.getLogger(OrderIntegrationServiceImpl.class);

    private final OrderRepository orderRepository;
    private final ExternalSystemClient externalSystemClient;
    private final Validator validator;

    public OrderIntegrationServiceImpl(OrderRepository orderRepository,
                                       ExternalSystemClient externalSystemClient,
                                       Validator validator) {
        this.orderRepository = orderRepository;
        this.externalSystemClient = externalSystemClient;
        this.validator = validator;
    }

    @Override
    public List<Order> importOrdersFromExternal(String endpoint) {
        try {
            logger.info("외부 시스템에서 주문 데이터 가져오기 시작: {}", endpoint);

            List<Order> orders = externalSystemClient.fetchOrders(endpoint);
            logger.info("가져온 주문 수: {}", orders.size());

            List<Order> savedOrders = orders.stream()
                    .filter(this::validateOrder)
                    .map(this::saveOrderSafely)
                    .filter(order -> order != null)
                    .collect(Collectors.toList());

            logger.info("저장된 주문 수: {}", savedOrders.size());
            return savedOrders;

        } catch (ExternalSystemException e) {
            logger.error("외부 시스템 연동 오류: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("주문 데이터 가져오기 중 오류 발생", e);
            throw new DataIntegrationException("주문 데이터 가져오기 실패", e);
        }
    }

    @Override
    public boolean exportOrderToExternal(String endpoint, String orderId) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new OrderNotFoundException(orderId));

            logger.info("주문 데이터 외부 시스템 전송 시작: {} -> {}", orderId, endpoint);

            boolean result = externalSystemClient.sendOrder(endpoint, order);

            if (result) {
                logger.info("주문 데이터 전송 완료: {}", orderId);
            } else {
                logger.warn("주문 데이터 전송 실패: {}", orderId);
            }

            return result;

        } catch (OrderNotFoundException e) {
            logger.error("주문을 찾을 수 없음: {}", orderId);
            throw e;
        } catch (ExternalSystemException e) {
            logger.error("외부 시스템 전송 오류: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("주문 데이터 전송 중 오류 발생", e);
            throw new DataIntegrationException("주문 데이터 전송 실패", e);
        }
    }

    @Override
    public boolean exportOrdersToExternal(String endpoint, List<String> orderIds) {
        try {
            List<Order> orders = orderIds.stream()
                    .map(orderId -> orderRepository.findById(orderId)
                            .orElse(null))
                    .filter(order -> order != null)
                    .collect(Collectors.toList());

            if (orders.isEmpty()) {
                throw new DataIntegrationException("전송할 유효한 주문이 없습니다");
            }

            logger.info("여러 주문 데이터 외부 시스템 전송 시작: {} 건 -> {}", orders.size(), endpoint);

            boolean result = externalSystemClient.sendOrders(endpoint, orders);

            if (result) {
                logger.info("여러 주문 데이터 전송 완료: {} 건", orders.size());
            } else {
                logger.warn("여러 주문 데이터 전송 실패: {} 건", orders.size());
            }

            return result;

        } catch (ExternalSystemException e) {
            logger.error("외부 시스템 전송 오류: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("여러 주문 데이터 전송 중 오류 발생", e);
            throw new DataIntegrationException("여러 주문 데이터 전송 실패", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrderById(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    private boolean validateOrder(Order order) {
        Set<ConstraintViolation<Order>> violations = validator.validate(order);
        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            logger.warn("주문 검증 실패 [{}]: {}", order.getOrderId(), errorMessage);
            return false;
        }
        return true;
    }

    private Order saveOrderSafely(Order order) {
        try {
            return orderRepository.save(order);
        } catch (Exception e) {
            logger.error("주문 저장 실패 [{}]: {}", order.getOrderId(), e.getMessage());
            return null;
        }
    }
}
