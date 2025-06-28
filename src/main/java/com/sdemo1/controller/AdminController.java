package com.sdemo1.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sdemo1.common.response.ApiResponse;
import com.sdemo1.service.CategoryService;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 카테고리 캐시 상태 확인
     */
    @GetMapping("/cache/status")
    public ApiResponse<String> getCacheStatus() {
        String status = categoryService.getCacheStatus();
        return new ApiResponse<>("캐시 상태 조회 완료", status, HttpStatus.OK);
    }

    /**
     * 카테고리 캐시 수동 갱신
     */
    @PostMapping("/cache/refresh")
    public ApiResponse<String> refreshCache() {
        try {
            categoryService.refreshCategoryMap();
            return new ApiResponse<>("캐시가 성공적으로 갱신되었습니다.", null, HttpStatus.OK);
        } catch (Exception e) {
            return new ApiResponse<>("캐시 갱신 중 오류 발생: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
} 