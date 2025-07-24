package com.humuson.orderintegration.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalSystemResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private String timestamp;
}
