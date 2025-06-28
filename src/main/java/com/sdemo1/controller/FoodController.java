package com.sdemo1.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.sdemo1.service.FoodService;
import com.sdemo1.service.FoodIngredientService;
import com.sdemo1.request.FoodIngredientRequest;
import com.sdemo1.response.FoodIngredientResponse;
import com.sdemo1.common.response.ApiResponse;
import com.sdemo1.dto.PageRequestDto;
import com.sdemo1.entity.FoodItem;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@RestController
@RequestMapping("/ck")
@RequiredArgsConstructor
public class FoodController {

    private final FoodService foodService;
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
     * 사용자의 음식재료 목록 조회 API (페이징) - findIngredientByFilter와 동일한 응답 구조
     */
    @GetMapping("/my-ingredient")
    public ApiResponse<Page<FoodItem>> getFoodIngredients(
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size) {
        try {
            // JWT 토큰에서 member_id 추출
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Integer memberId = Integer.parseInt(auth.getName());

            // 페이징 처리
            PageRequestDto pageRequestDto = new PageRequestDto(page, size);
            Page<FoodItem> ingredients = foodIngredientService.findIngredientsByMemberId(memberId, pageRequestDto);

            return new ApiResponse<>(null, ingredients, HttpStatus.OK);

        } catch (Exception e) {
            return new ApiResponse<>("음식재료 목록 조회 중 오류가 발생했습니다: " + e.getMessage(), 
                                   null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
