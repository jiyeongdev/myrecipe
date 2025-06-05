package com.sdemo1.exception;

public class CustomException extends RuntimeException {
    private final int statusCode;
    private final String detailMessage;

    public CustomException(String message, String detailMessage, int statusCode) {
        super(message);
        this.detailMessage = detailMessage;
        this.statusCode = statusCode;
    }

    public CustomException(String message, int statusCode) {
        super(message);
        this.detailMessage = null;
        this.statusCode = statusCode;
    }

    public CustomException(String message) {
        super(message);
        this.detailMessage = null;
        this.statusCode = 400; // Default to BAD_REQUEST
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getDetailMessage() {
        return detailMessage;
    }
} 