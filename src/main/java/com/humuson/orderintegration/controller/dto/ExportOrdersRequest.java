package com.humuson.orderintegration.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExportOrdersRequest {
    @NotBlank(message = "엔드포인트는 필수입니다")
    private String endpoint;

    @NotNull(message = "주문 ID 목록은 필수입니다")
    private List<String> orderIds;
}
