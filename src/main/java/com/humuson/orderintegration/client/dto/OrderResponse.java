package com.humuson.orderintegration.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private String orderId;
    private String customerName;
    private String orderDate;
    private String status;
    private String description;
    private String processedAt;
}
