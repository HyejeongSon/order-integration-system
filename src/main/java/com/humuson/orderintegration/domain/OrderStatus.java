package com.humuson.orderintegration.domain;

public enum OrderStatus {
    PROCESSING("처리 중"),
    SHIPPING("배송 중"),
    COMPLETED("완료"),
    CANCELLED("취소");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
