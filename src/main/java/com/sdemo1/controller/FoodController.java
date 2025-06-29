package com.sdemo1.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.sdemo1.service.FoodService;
import com.sdemo1.service.FoodIngredientService;
import com.sdemo1.request.FoodIngredientRequest;
import com.sdemo1.request.BulkDeleteIngredientRequest;
import com.sdemo1.common.response.ApiResponse;
import com.sdemo1.entity.FoodItem;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/ck")
@RequiredArgsConstructor
public class FoodController {

    private final FoodIngredientService foodIngredientService;

    /**
     * 음식재료 등록 API (배열) - Bulk Insert
     */
    @PostMapping("/my-ingredient")
    public ResponseEntity<ApiResponse<String>> createFoodIngredients(
            @RequestBody List<FoodIngredientRequest> requests) {
        try {
            // JWT 토큰에서 member_id 추출
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Integer memberId = Integer.parseInt(auth.getName());

            String result = foodIngredientService.createFoodIngredients(memberId, requests);
            
            return ResponseEntity.ok()
                    .body(new ApiResponse<>(result, null, HttpStatus.OK));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("음식재료 등록 중 오류가 발생했습니다: " + e.getMessage(), 
                                          null, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 사용자의 음식재료 목록 조회 API (전체 조회)
     */
    @GetMapping("/my-ingredient")
    public ApiResponse<List<FoodItem>> getFoodIngredients() {
        try {
            // JWT 토큰에서 member_id 추출
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Integer memberId = Integer.parseInt(auth.getName());

            // 전체 조회
            List<FoodItem> ingredients = foodIngredientService.findAllIngredientsByMemberId(memberId);

            return new ApiResponse<>(null, ingredients, HttpStatus.OK);

        } catch (Exception e) {
            return new ApiResponse<>("음식재료 목록 조회 중 오류가 발생했습니다: " + e.getMessage(), 
                                   null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 음식재료 삭제 API
     */
    @DeleteMapping("/my-ingredient")
    public ResponseEntity<ApiResponse<String>> deleteFoodIngredients(
            @RequestBody List<Integer> foodIds) {
        try {
            // JWT 토큰에서 member_id 추출
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Integer memberId = Integer.parseInt(auth.getName());

            String result = foodIngredientService.deleteFoodIngredients(memberId, foodIds);

            return ResponseEntity.ok()
                    .body(new ApiResponse<>(result, null, HttpStatus.OK));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("음식재료 삭제 중 오류가 발생했습니다: " + e.getMessage(), 
                                          null, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

}
