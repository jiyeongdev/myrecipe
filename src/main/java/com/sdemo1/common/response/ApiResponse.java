package com.sdemo1.common.response;

import org.springframework.http.HttpStatus;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private String message;
    private T data;
    private int statusCode;

    public ApiResponse(String message, T data, HttpStatus status) {
        this.message = message;
        this.statusCode = status.value();
        this.data = data;
    }
    
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(null, data, HttpStatus.OK);
    }
    
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(message, null, HttpStatus.BAD_REQUEST);
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