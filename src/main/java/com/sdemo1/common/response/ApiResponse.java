package com.sdemo1.common.response;

import org.springframework.http.HttpStatus;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private int statusCode;

    public ApiResponse(boolean success, String message, T data, HttpStatus status) {
        this.success = success;
        this.message = message;
        this.statusCode = status.value();
        this.data = data;
        
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public int getStatusCode() {
        return statusCode;
    }
} 