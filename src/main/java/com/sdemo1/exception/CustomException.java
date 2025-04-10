package com.sdemo1.exception;

public class CustomException extends RuntimeException {
    private final int statusCode;

    public CustomException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public CustomException(String message) {
        super(message);
        this.statusCode = 400; // Default to BAD_REQUEST
    }

    public int getStatusCode() {
        return statusCode;
    }
} 