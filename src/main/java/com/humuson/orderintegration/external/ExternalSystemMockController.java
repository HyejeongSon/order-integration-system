package com.humuson.orderintegration.external;

import com.humuson.orderintegration.client.dto.ExternalSystemResponse;
import com.humuson.orderintegration.client.dto.OrderRequest;
import com.humuson.orderintegration.client.dto.OrderResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * 외부 시스템 테스트용 컨트롤러로, 실제 외부 시스템의 동작을 시뮬레이션한다.
 */
@RestController
@RequestMapping("/external-system")
public class ExternalSystemMockController {

    private static final Logger logger = LoggerFactory.getLogger(ExternalSystemMockController.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 외부 시스템에서 주문 데이터를 제공하는 엔드포인트 (Import 테스트용)
     * GET /external-system/orders
     */
    @GetMapping("/orders")
    public ResponseEntity<List<OrderRequest>> getOrders() {
        logger.info("외부 시스템: 주문 데이터 요청 수신");

        // 테스트용 주문 데이터 생성
        List<OrderRequest> orders = Arrays.asList(
                OrderRequest.builder()
                        .orderId("EXT-ORDER-001")
                        .customerName("김철수")
                        .orderDate("2024-01-15 09:30:00")
                        .status("PROCESSING")
                        .description("외부 시스템 주문 1")
                        .build(),
                OrderRequest.builder()
                        .orderId("EXT-ORDER-002")
                        .customerName("이영희")
                        .orderDate("2024-01-15 10:15:00")
                        .status("SHIPPING")
                        .description("외부 시스템 주문 2")
                        .build(),
                OrderRequest.builder()
                        .orderId("EXT-ORDER-003")
                        .customerName("박민수")
                        .orderDate("2024-01-15 11:00:00")
                        .status("COMPLETED")
                        .description("외부 시스템 주문 3")
                        .build()
        );

        logger.info("외부 시스템: {} 건의 주문 데이터 전송", orders.size());
        return ResponseEntity.ok(orders);
    }

    /**
     * 외부 시스템에서 주문 데이터를 수신하는 엔드포인트 (Export 테스트용)
     * POST /external-system/orders
     */
    @PostMapping("/orders")
    public ResponseEntity<ExternalSystemResponse<String>> receiveOrders(
            @RequestBody List<OrderResponse> orders) {

        logger.info("외부 시스템: {} 건의 주문 데이터 수신", orders.size());

        // 수신된 주문 데이터 로깅
        for (OrderResponse order : orders) {
            logger.info("수신된 주문: ID={}, 고객명={}, 상태={}, 처리시간={}",
                    order.getOrderId(),
                    order.getCustomerName(),
                    order.getStatus(),
                    order.getProcessedAt());
        }

        // 성공 응답 반환
        ExternalSystemResponse<String> response = ExternalSystemResponse.<String>builder()
                .success(true)
                .message(orders.size() + "건의 주문 데이터를 성공적으로 수신했습니다.")
                .data("처리 완료")
                .timestamp(LocalDateTime.now().format(DATE_FORMATTER))
                .build();

        logger.info("외부 시스템: 주문 데이터 처리 완료");
        return ResponseEntity.ok(response);
    }

    /**
     * 네트워크 오류 시뮬레이션 엔드포인트
     * GET /external-system/orders/error
     */
    @GetMapping("/orders/error")
    public ResponseEntity<String> simulateError() {
        logger.warn("외부 시스템: 오류 시뮬레이션 - 500 Internal Server Error");
        return ResponseEntity.status(500).body("External System Error: Database connection failed");
    }

    /**
     * 지연 응답 시뮬레이션 엔드포인트
     * GET /external-system/orders/slow
     */
    @GetMapping("/orders/slow")
    public ResponseEntity<List<OrderRequest>> getOrdersWithDelay() {
        logger.info("외부 시스템: 지연 응답 시뮬레이션 시작");

        try {
            // 5초 지연
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("지연 시뮬레이션 중 인터럽트 발생", e);
        }

        List<OrderRequest> orders = Arrays.asList(
                OrderRequest.builder()
                        .orderId("SLOW-ORDER-001")
                        .customerName("지연테스트")
                        .orderDate("2024-01-15 12:00:00")
                        .status("PROCESSING")
                        .description("지연 응답 테스트 주문")
                        .build()
        );

        logger.info("외부 시스템: 지연 응답 완료");
        return ResponseEntity.ok(orders);
    }

    /**
     * 잘못된 데이터 형식 시뮬레이션 엔드포인트
     * GET /external-system/orders/invalid
     */
    @GetMapping("/orders/invalid")
    public ResponseEntity<String> getInvalidData() {
        logger.warn("외부 시스템: 잘못된 데이터 형식 반환");
        return ResponseEntity.ok("{ \"invalid\": \"json format\" }");
    }

    /**
     * 빈 데이터 시뮬레이션 엔드포인트
     * GET /external-system/orders/empty
     */
    @GetMapping("/orders/empty")
    public ResponseEntity<List<OrderRequest>> getEmptyOrders() {
        logger.info("외부 시스템: 빈 주문 데이터 반환");
        return ResponseEntity.ok(Arrays.asList());
    }

    /**
     * 부분적 실패 시뮬레이션 엔드포인트
     * POST /external-system/orders/partial-fail
     */
    @PostMapping("/orders/partial-fail")
    public ResponseEntity<ExternalSystemResponse<String>> receiveOrdersWithPartialFailure(
            @RequestBody List<OrderResponse> orders) {

        logger.warn("외부 시스템: 부분적 실패 시뮬레이션");

        ExternalSystemResponse<String> response = ExternalSystemResponse.<String>builder()
                .success(false)
                .message("일부 주문 처리에 실패했습니다. 성공: " + (orders.size() - 1) + "건, 실패: 1건")
                .data("부분적 처리 완료")
                .timestamp(LocalDateTime.now().format(DATE_FORMATTER))
                .build();

        return ResponseEntity.status(207).body(response); // 207 Multi-Status
    }

}
