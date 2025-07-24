package com.humuson.orderintegration.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @NotBlank(message = "주문 ID는 필수입니다")
    private String orderId;

    @NotBlank(message = "고객명은 필수입니다")
    private String customerName;

    @NotNull(message = "주문 날짜는 필수입니다")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime orderDate;

    @NotNull(message = "주문 상태는 필수입니다")
    private OrderStatus status;

    private String description;

}
