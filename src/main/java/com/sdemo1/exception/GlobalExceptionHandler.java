package com.sdemo1.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.sdemo1.common.response.ApiResponse;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<?>> handleCustomException(CustomException e) {
        Map<String, String> errorDetails = new HashMap<>();
        errorDetails.put("message", e.getMessage());
        if (e.getDetailMessage() != null) {
            errorDetails.put("detail", e.getDetailMessage());
        }
        
        ApiResponse<?> response = new ApiResponse<>(
            e.getMessage(),
            errorDetails,
            HttpStatus.valueOf(e.getStatusCode())
        );
        
        return ResponseEntity
            .status(e.getStatusCode())
            .body(response);
    }
} 