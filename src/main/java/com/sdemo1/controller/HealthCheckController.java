package com.sdemo1.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sdemo1.common.response.ApiResponse;

@RestController
public class HealthCheckController {

    @GetMapping("/health-check")
    public ApiResponse<String> healthCheck() {
        return new ApiResponse<>(
        
            "서버가 정상적으로 동작중입니다.",
            "OK",
            HttpStatus.OK
        );
    }
} 