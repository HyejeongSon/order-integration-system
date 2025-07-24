package com.humuson.orderintegration.exception;

public class DataIntegrationException extends RuntimeException {
    public DataIntegrationException(String message) {
        super(message);
    }

    public DataIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
