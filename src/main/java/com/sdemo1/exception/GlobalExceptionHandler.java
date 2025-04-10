package com.sdemo1.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.sdemo1.common.response.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ApiResponse<?> handleCustomException(CustomException e) {
        return new ApiResponse<>(
            false,
            e.getMessage(),  // 에러 메시지를 응답에 포함
            null,
            HttpStatus.valueOf(e.getStatusCode())
        );
    }
} 