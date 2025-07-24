package com.humuson.orderintegration.exception;

public class ExternalSystemException extends DataIntegrationException {
    private final String systemType;

    public ExternalSystemException(String systemType, String message) {
        super(String.format("[%s] %s", systemType, message));
        this.systemType = systemType;
    }

    public ExternalSystemException(String systemType, String message, Throwable cause) {
        super(String.format("[%s] %s", systemType, message), cause);
        this.systemType = systemType;
    }

    public String getSystemType() {
        return systemType;
    }
}
