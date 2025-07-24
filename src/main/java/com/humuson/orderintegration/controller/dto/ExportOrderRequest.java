package com.humuson.orderintegration.controller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExportOrderRequest {
    @NotBlank(message = "엔드포인트는 필수입니다")
    private String endpoint;
}
