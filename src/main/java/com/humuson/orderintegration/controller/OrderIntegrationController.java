package com.humuson.orderintegration.controller;

import com.humuson.orderintegration.controller.dto.ApiResponse;
import com.humuson.orderintegration.controller.dto.ExportOrderRequest;
import com.humuson.orderintegration.controller.dto.ExportOrdersRequest;
import com.humuson.orderintegration.controller.dto.ImportOrdersRequest;
import com.humuson.orderintegration.domain.Order;
import com.humuson.orderintegration.domain.OrderStatus;
import com.humuson.orderintegration.exception.OrderNotFoundException;
import com.humuson.orderintegration.service.OrderIntegrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderIntegrationController {

    private final OrderIntegrationService orderIntegrationService;

    public OrderIntegrationController(OrderIntegrationService orderIntegrationService) {
        this.orderIntegrationService = orderIntegrationService;
    }

    /**
     * 외부 시스템에서 주문 데이터 가져오기
     */
    @PostMapping("/import")
    public ResponseEntity<ApiResponse<List<Order>>> importOrders(
            @Valid @RequestBody ImportOrdersRequest request) {
        try {
            List<Order> orders = orderIntegrationService.importOrdersFromExternal(request.getEndpoint());
            return ResponseEntity.ok(ApiResponse.success("주문 데이터 가져오기 완료", orders));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("주문 데이터 가져오기 실패: " + e.getMessage()));
        }
    }

    /**
     * 주문 데이터를 외부 시스템으로 전송
     */
    @PostMapping("/export/{orderId}")
    public ResponseEntity<ApiResponse<Boolean>> exportOrder(
            @PathVariable String orderId,
            @Valid @RequestBody ExportOrderRequest request) {
        try {
            boolean result = orderIntegrationService.exportOrderToExternal(request.getEndpoint(), orderId);
            return ResponseEntity.ok(ApiResponse.success("주문 데이터 전송 완료", result));
        } catch (OrderNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("주문 데이터 전송 실패: " + e.getMessage()));
        }
    }

    /**
     * 여러 주문 데이터를 외부 시스템으로 전송
     */
    @PostMapping("/export")
    public ResponseEntity<ApiResponse<Boolean>> exportOrders(
            @Valid @RequestBody ExportOrdersRequest request) {
        try {
            boolean result = orderIntegrationService.exportOrdersToExternal(
                    request.getEndpoint(), request.getOrderIds());
            return ResponseEntity.ok(ApiResponse.success("여러 주문 데이터 전송 완료", result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("여러 주문 데이터 전송 실패: " + e.getMessage()));
        }
    }

    /**
     * 모든 주문 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Order>>> getAllOrders() {
        List<Order> orders = orderIntegrationService.getAllOrders();
        return ResponseEntity.ok(ApiResponse.success("주문 목록 조회 완료", orders));
    }

    /**
     * 특정 주문 조회
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<Order>> getOrder(@PathVariable String orderId) {
        try {
            Order order = orderIntegrationService.getOrderById(orderId);
            return ResponseEntity.ok(ApiResponse.success("주문 조회 완료", order));
        } catch (OrderNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 상태별 주문 조회
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<Order>>> getOrdersByStatus(@PathVariable OrderStatus status) {
        List<Order> orders = orderIntegrationService.getOrdersByStatus(status);
        return ResponseEntity.ok(ApiResponse.success("상태별 주문 조회 완료", orders));
    }
}
